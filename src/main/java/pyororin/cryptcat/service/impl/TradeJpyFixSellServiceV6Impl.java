package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.config.OrderLogic;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckOpensOrdersResponse;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.CoinCheckResponse;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.TradeService;

import java.time.Clock;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeJpyFixSellServiceV6Impl implements TradeService {
    private final Clock clock;
    private final TradeRateLogicService tradeRateLogicService;
    private final CoinCheckRepository repository;

    @Override
    public void order(Pair pair, OrderRequest orderRequest) {
        // 非同期タスクで処理を実行
        CompletableFuture.runAsync(() -> processOrderWithRetry(pair, orderRequest, UUID.randomUUID().toString().split("-")[0]));
    }

    private void processOrderWithRetry(Pair pair, OrderRequest orderRequest, String uuid) {
        // OrderLogicをcancelDelayMinutesで昇順にソート
        List<OrderLogic> sortedOrderLogics = Arrays.stream(OrderLogic.values())
                .sorted(Comparator.comparingInt(OrderLogic::getCancelDelayMinutes))
                .toList();

        for (OrderLogic orderLogic : sortedOrderLogics) {
            log.info("{} {} {} {}", value("kind", "order-v6"), value("trace-id", uuid),
                    value("action", "attempt-sell"), value("order-logic", orderLogic));
            var btc = repository.retrieveBalance().getBtc();
            if (btc.doubleValue() <= 0.001) {
                log.info("{} {} {} {}", value("kind", "order-v6"), value("trace-id", uuid),
                        value("jpy", btc), value("action", "order-skip"));
                return;
            }
            var sellPrice = tradeRateLogicService.selectSellPrice(pair, orderLogic);
            /* 市場最終価格(ticker.last or ticker.ask) = rate */
            /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
            var response = repository.exchangeSellLimit(CoinCheckRequest.builder()
                    .pair(pair)
                    .amount(btc)
                    .rate(sellPrice)
                    .group(orderRequest.getGroup())
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
