package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.config.OrderStatus;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.*;
import pyororin.cryptcat.service.TradeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static net.logstash.logback.argument.StructuredArguments.value;

/**
 * 手持ち資金を全て売買するサービス v2
 * ・手持ち全てのJPYで購入
 * ・前回売却時より上昇している場合のみ続行（現時点では凍結）
 * ・15秒おきに order-cancel を終わるまで繰り返す（最大10回まで）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeAllInBuyServiceV2Impl implements TradeService {
    private final Clock clock;
    private final TradeRateLogicService tradeRateLogicService;
    private final CoinCheckRepository repository;
    private final OrderTransactionService orderTransactionService;

    @Override
    public void order(Pair pair, OrderRequest orderRequest) {
        // 非同期タスクで処理を実行
        CompletableFuture.runAsync(() -> processOrderWithRetry(pair, orderRequest, UUID.randomUUID().toString().split("-")[0]));
    }

    private void processOrderWithRetry(Pair pair, OrderRequest orderRequest, String uuid) {
        var isOrderStopped = new AtomicBoolean(false);
        IntStream.range(0, 10).takeWhile(__ -> !isOrderStopped.get()).forEach(i -> {
            log.info("{} {} {} {}", value("kind", "order-v6"), value("trace-id", uuid),
                    value("action", "attempt-buy"), value("retry", i));
            var jpy = repository.retrieveBalance().getJpy().subtract(BigDecimal.valueOf(7777));
            var buyPrice = tradeRateLogicService.getFairBuyPrice(pair);
//            var beforePrice = orderTransactionService.get("All-In-Sell").getOrderId();

            // 購入出来ない場合は見送り
            if (jpy.longValue() <= 0) {
                log.info("{} {} {} {}", value("kind", "order-allin-v2"), value("trace-id", uuid),
                        value("jpy", jpy), value("action", "order-skip"));
                isOrderStopped.set(true);
                return;
            }
//            // 前回売却時点よりRateが高い場合は見送り
//            if (buyPrice.longValue() >= beforePrice && beforePrice != 0) {
//                log.info("{} {} {} {} {}", value("kind", "order-allin-v2"), value("trace-id", uuid),
//                        value("buy-price", buyPrice), value("before-price", beforePrice), value("action", "order-skip"));
//                isOrderStopped.set(true);
//                return;
//            }

            var amount = jpy.divide(buyPrice, 9, RoundingMode.DOWN);
            var response = repository.exchangeBuyLimit(CoinCheckRequest.builder()
                    .pair(pair)
                    .amount(amount)
                    .rate(buyPrice)
                    .group(orderRequest.getGroup())
                    .build());

            // `OrderLogic`に基づくキャンセル待ち時間を渡す
            if (waitForOrderConfirmationOrCancel(response, uuid)) {
                // 注文が確定した場合、処理を終了
                log.info("{} {} {}", value("kind", "order-allin-v2"), value("trace-id", uuid),
                        value("action", "order-confirmed"));
                isOrderStopped.set(true);
            }

            orderTransactionService.set("All-In-Buy", OrderTransaction.builder()
                .orderId(Long.valueOf(response.getRate()))
                .orderStatus(OrderStatus.ORDERED)
                .createdAt(Objects.isNull(response.getCreatedAt())
                        ? DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.from(ZoneOffset.UTC)).format(clock.instant())
                        : response.getCreatedAt())
                .orderType(OrderType.BUY)
                .build());
        });
    }

    private boolean waitForOrderConfirmationOrCancel(CoinCheckResponse response, String uuid) {
        var isOrderConfirmed = new CompletableFuture<Boolean>();
        var scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutor.scheduleWithFixedDelay(() -> {
            var opensOrdersIds = repository.retrieveOpensOrders()
                    .findOrdersWithinMinuets(clock, 0, 1)
                    .stream().map(CoinCheckOpensOrdersResponse.Order::getId).toList();

            log.info("{} {} {} {} {}", value("kind", "order-allin-v2"), value("trace-id", uuid),
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
                log.info("{} {} {} {}", value("kind", "order-allin-v2"), value("trace-id", uuid),
                        value("action", "order-cancelled"),
                        value("order-transaction", response));
            }
            isOrderConfirmed.complete(false);
            scheduledExecutor.shutdown();
        }, 15, TimeUnit.SECONDS);

        // 結果が確定するまで待機（非同期処理でブロックせず結果を返す）
        try {
            return isOrderConfirmed.get();
        } catch (Exception e) {
            log.error("{} {} {}", value("kind", "order-allin-v2"), value("trace-id", uuid),
                    value("action", "error-in-order-confirmation"), e);
            return false;
        }
    }
}
