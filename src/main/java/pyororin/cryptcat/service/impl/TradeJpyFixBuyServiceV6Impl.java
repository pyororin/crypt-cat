package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.config.OrderLogic;
import pyororin.cryptcat.config.OrderStatus;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.*;
import pyororin.cryptcat.service.TradeService;

import java.math.RoundingMode;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeJpyFixBuyServiceV6Impl implements TradeService {
    private final Clock clock;
    private final TradeRateLogicService tradeRateLogicService;
    private final CoinCheckRepository repository;
    private final CoinCheckApiConfig apiConfig;
    private final OrderTransactionService orderTransactionService;

    @Override
    public void order(Pair pair, OrderRequest orderRequest) {
        var uuid = UUID.randomUUID().toString().split("-")[0];
        var transaction = orderTransactionService.get(orderRequest.getGroup());

        if (transaction.isBuySkip()) {
            log.info("{} {} {} {}", value("kind", "order-v6"), value("trace-id", uuid),
                    value("action", "skip-buy"),
                    value("order-transaction", orderTransactionService.get(orderRequest.getGroup())));
            orderTransactionService.remove(orderRequest.getGroup());
            return;
        }

        // 非同期タスクで処理を実行
        CompletableFuture.runAsync(() -> processOrderWithRetry(pair, orderRequest, uuid));
    }

    private void processOrderWithRetry(Pair pair, OrderRequest orderRequest, String uuid) {
        // OrderLogicをcancelDelayMinutesで昇順にソート
        List<OrderLogic> sortedOrderLogics = Arrays.stream(OrderLogic.values())
                .sorted(Comparator.comparingInt(OrderLogic::getCancelDelayMinutes))
                .toList();

        for (OrderLogic orderLogic : sortedOrderLogics) {
            log.info("{} {} {} {}", value("kind", "order-v6"), value("trace-id", uuid),
                    value("action", "attempt-buy"), value("order-logic", orderLogic));

            var buyPrice = tradeRateLogicService.selectBuyPrice(pair, orderLogic);
            var amount = repository.retrieveBalance().getJpy().divide(buyPrice, 9, RoundingMode.DOWN);
            var response = repository.exchangeBuyLimit(CoinCheckRequest.builder()
                    .pair(pair)
                    .price(apiConfig.getPrice().multiply(orderRequest.getRatio()))
                    .amount(amount)
                    .rate(buyPrice)
                    .group(orderRequest.getGroup())
                    .build());

            orderTransactionService.set(orderRequest.getGroup(), OrderTransaction.builder()
                    .orderId(response.getId())
                    .orderStatus(OrderStatus.ORDERED)
                    .createdAt(Objects.isNull(response.getCreatedAt())
                            ? DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.from(ZoneOffset.UTC)).format(clock.instant())
                            : response.getCreatedAt())
                    .orderType(OrderType.BUY)
                    .build());

            // `OrderLogic`に基づくキャンセル待ち時間を渡す
            if (waitForOrderConfirmationOrCancel(response, orderRequest, uuid, orderLogic.getCancelDelayMinutes())) {
                // 注文が確定した場合、処理を終了
                log.info("{} {} {}", value("kind", "order-v6"), value("trace-id", uuid),
                        value("action", "order-confirmed"));
                return;
            }
        }

        log.warn("{} {} {} {}", value("kind", "order-v6"), value("trace-id", uuid),
                value("action", "max-retry-failed"),
                value("order-transaction", orderRequest.getGroup()));
    }

    private boolean waitForOrderConfirmationOrCancel(CoinCheckResponse response, OrderRequest orderRequest, String uuid, int cancelDelayMinutes) {
        var isOrderConfirmed = new CompletableFuture<Boolean>();
        var scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutor.scheduleWithFixedDelay(() -> {
            var opensOrdersIds = repository.retrieveOpensOrders()
                    .findOrdersWithinMinuets(clock, 0, (long) (cancelDelayMinutes * 1.1))
                    .stream().map(CoinCheckOpensOrdersResponse.Order::getId).toList();

            log.info("{} {} {} {} {}", value("kind", "order-v6"), value("trace-id", uuid),
                    value("action", "check-open-orders"),
                    value("order-id", response.getId()), value("opens-ids", opensOrdersIds));

            if (!opensOrdersIds.contains(response.getId())) {
                // 注文が確定した場合
                isOrderConfirmed.complete(true);
                scheduledExecutor.shutdown();
            }
        }, 15, 15, TimeUnit.SECONDS);

        // キャンセル処理を一定時間後にスケジュール
        scheduledExecutor.schedule(() -> {
            if (!isOrderConfirmed.isDone() && repository.exchangeCancel(response.getId())) {
                orderTransactionService.set(orderRequest.getGroup(), OrderTransaction.builder()
                        .orderId(response.getId())
                        .orderStatus(OrderStatus.CANCEL)
                        .createdAt(Objects.isNull(response.getCreatedAt())
                                ? DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.from(ZoneOffset.UTC)).format(clock.instant())
                                : response.getCreatedAt())
                        .orderType(orderRequest.isBuy() ? OrderType.BUY : OrderType.SELL)
                        .build());
                log.info("{} {} {} {}", value("kind", "order-v6"), value("trace-id", uuid),
                        value("action", "order-cancelled"),
                        value("order-transaction", response));
            }
            isOrderConfirmed.complete(false);
            scheduledExecutor.shutdown();
        }, cancelDelayMinutes, TimeUnit.MINUTES); // OrderLogicに基づくキャンセル待ち時間を適用

        // 結果が確定するまで待機（非同期処理でブロックせず結果を返す）
        try {
            return isOrderConfirmed.get();
        } catch (Exception e) {
            log.error("{} {} {}", value("kind", "order-v6"), value("trace-id", uuid),
                    value("action", "error-in-order-confirmation"), e);
            return false;
        }
    }
}
