package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.config.OrderStatus;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.*;
import pyororin.cryptcat.service.TradeService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static net.logstash.logback.argument.StructuredArguments.value;

/**
 * 手持ち資金を全て売買するサービス v2
 * ・手持ちのBTCを全て売却
 * ・前回売却時より上昇している場合のみ続行
 * ・15秒おきに order-cancel を終わるまで繰り返す（最大10回まで）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeAllInSellServiceV2Impl implements TradeService {
    private final Clock clock;
    private final CoinCheckApiConfig.Retry apiConfigRetry;
    private final TradeRateLogicService tradeRateLogicService;
    private final CoinCheckRepository repository;
    private final OrderTransactionService orderTransactionService;

    @Override
    public void order(Pair pair, OrderRequest orderRequest) {
        // 非同期タスクで処理を実行
        CompletableFuture.runAsync(() -> processOrderWithRetry(pair, orderRequest, UUID.randomUUID().toString().split("-")[0]))
                .exceptionally(ex -> {
                    log.error(ex.getLocalizedMessage(), ex);
                    return null;
                });
    }

    private void processOrderWithRetry(Pair pair, OrderRequest orderRequest, String uuid) {
        var isOrderStopped = new AtomicBoolean(false);
        var isOrderSkipped = new AtomicBoolean(false);
        var firstOrderRate = new AtomicLong();
        var lastOrderRate = new AtomicLong();
        IntStream.range(0, apiConfigRetry.getLimitCount()).takeWhile(__ -> !isOrderStopped.get() && !isOrderSkipped.get()).forEach(i -> {
            log.info("{} {} {} {}", value("kind", "order-allin-v2"), value("trace-id", uuid),
                    value("action", "attempt-sell"), value("retry", i));
            var btc = repository.retrieveBalance().getBtc();
            var sellRate = tradeRateLogicService.getFairSellRate(pair);
            var beforeRate = Optional.ofNullable(orderTransactionService.get("All-In-Buy").getOrderId()).orElse(0L);

            // 売却出来ない場合は見送り
            if (btc.doubleValue() <= 0.001) {
                log.info("{} {} {} {}", value("kind", "order-allin-v2"), value("trace-id", uuid),
                        value("reason", String.format("%f <= 0.001 btc", btc)), value("action", "sell-skip"));
                orderTransactionService.addSkipCount("All-In-Sell");
                isOrderSkipped.set(true);
                return;
            }
            // 前回購入時点よりRateが低い場合は見送り
            if (sellRate.longValue() <= beforeRate) {
                log.info("{} {} {} {}", value("kind", "order-allin-v2"), value("trace-id", uuid),
                        value("reason", String.format("%d <= %d", sellRate.longValue(), beforeRate)), value("action", "sell-skip"));
                orderTransactionService.addSkipCount("All-In-Sell");
                isOrderSkipped.set(true);
                return;
            }

            /* 市場最終価格(ticker.last or ticker.ask) = rate */
            /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
            var response = repository.exchangeSellLimit(CoinCheckRequest.builder()
                    .pair(pair)
                    .amount(btc)
                    .rate(sellRate)
                    .group(orderRequest.getGroup())
                    .build());

            // `OrderLogic`に基づくキャンセル待ち時間を渡す
            if (waitForOrderConfirmationOrCancel(response, uuid)) {
                // 注文が確定した場合、処理を終了
                isOrderStopped.set(true);
            }

            orderTransactionService.set("All-In-Sell", OrderTransaction.builder()
                    .orderId(response.getId())
                    .orderStatus(OrderStatus.ORDERED)
                    .createdAt(Objects.isNull(response.getCreatedAt())
                            ? DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.from(ZoneOffset.UTC)).format(clock.instant())
                            : response.getCreatedAt())
                    .orderType(OrderType.SELL)
                    .price(new BigDecimal(response.getRate()).longValue())
                    .skipCount(0L)
                    .build());
            if (firstOrderRate.get() == 0) { firstOrderRate.set(sellRate.longValue()); }
            lastOrderRate.set(sellRate.longValue());
        });

        // リトライに応じてどの程度期待値が低下したかロギング
        if (!isOrderSkipped.get()) {
            log.info("{} {} {} {} {} {}", value("kind", "order-allin-v2"), value("trace-id", uuid),
                    value("action", "diff"), value("first-order-rate", firstOrderRate.get()),
                    value("last-order-rate", lastOrderRate.get()),
                    value("diff-sell-rate", lastOrderRate.get() - firstOrderRate.get()));
        }
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

            if (opensOrdersIds.contains(response.getId())) {
                // 注文が未確定の場合
                if (repository.exchangeCancel(response.getId())) {
                    log.info("{} {} {} {}", value("kind", "order-allin-v2"), value("trace-id", uuid),
                            value("action", "order-cancelled"),
                            value("order-transaction", response));
                    isOrderConfirmed.complete(false);
                    scheduledExecutor.shutdown();
                }
            } else {
                // 注文が確定した場合
                log.info("{} {} {} {}", value("kind", "order-allin-v2"), value("trace-id", uuid),
                        value("action", "order-confirmed"),
                        value("order-transaction", response));
                isOrderConfirmed.complete(true);
                scheduledExecutor.shutdown();
            }
        }, 0, apiConfigRetry.getDelaySec(), TimeUnit.SECONDS);

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
