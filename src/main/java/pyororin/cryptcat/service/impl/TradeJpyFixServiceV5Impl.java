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

import java.math.RoundingMode;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeJpyFixServiceV5Impl implements TradeService {
    private final Clock clock;
    private final TradeRateLogicService tradeRateLogicService;
    private final CoinCheckRepository repository;
    private final CoinCheckApiConfig apiConfig;
    private final OrderTransactionService orderTransactionService;

    @Override
    public void order(Pair pair, OrderRequest orderRequest) {
        if (!orderRequest.isBuy() && !orderRequest.isSell()) {
            log.warn("BodyパラメータにorderTypeが無いか、buy|sell ではありません");
            return;
        }
        exchange(pair, orderRequest);
    }

    private void exchange(Pair pair, OrderRequest orderRequest) {
        var uuid = UUID.randomUUID().toString().split("-")[0];
        var transaction = orderTransactionService.get(orderRequest.getGroup());
        CoinCheckResponse response;
        if (orderRequest.isBuy()) {
            if (transaction.isBuySkip()) {
                log.info("{} {} {} {}", value("kind", "order-v5"), value("trace-id", uuid),
                        value("action", "skip-buy"),
                        value("order-transaction", orderTransactionService.get(orderRequest.getGroup())));
                    orderTransactionService.remove(orderRequest.getGroup());
                return;
            } else {
                var buyPrice = tradeRateLogicService.getFairBuyPrice(pair);
                /* 市場最終価格(ticker.last or ticker.ask) = rate */
                /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
                var amount = repository.retrieveBalance().getJpy().multiply(orderRequest.getRatio()).divide(buyPrice, 9, RoundingMode.DOWN);
                response = repository.exchangeBuyLimit(CoinCheckRequest.builder()
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
            }
        } else {
            if (transaction.isSellSkip()) {
                log.info("{} {} {} {}", value("kind", "order-v5"), value("trace-id", uuid),
                        value("action", "skip-sell"),
                        value("order-transaction", orderTransactionService.get(orderRequest.getGroup())));
                    orderTransactionService.remove(orderRequest.getGroup());
                return;
            } else {
                var sellPrice = tradeRateLogicService.getFairSellPrice(pair);
                /* 市場最終価格(ticker.last or ticker.ask) = rate */
                /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
                response = repository.exchangeSellLimit(CoinCheckRequest.builder()
                        .pair(pair)
                        .price(apiConfig.getPrice().multiply(orderRequest.getRatio()))
                        .amount(repository.retrieveBalance().getBtc())
                        .rate(sellPrice)
                        .group(orderRequest.getGroup())
                        .build());
                orderTransactionService.set(orderRequest.getGroup(), OrderTransaction.builder()
                        .orderId(response.getId())
                        .orderStatus(OrderStatus.ORDERED)
                        .createdAt(Objects.isNull(response.getCreatedAt())
                                ? DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.from(ZoneOffset.UTC)).format(clock.instant())
                                : response.getCreatedAt())
                        .orderType(OrderType.SELL)
                        .build());
            }
        }

        // 一定時間注文確認
        var executors = Executors.newScheduledThreadPool(1);
        var isNeedCancel = new AtomicBoolean(true);
        executors.scheduleWithFixedDelay(() -> {
            var opensOrdersIds = repository.retrieveOpensOrders().findOrdersWithinMinuets(clock, 0, (long) (apiConfig.getCancelDelayMinutes() * 1.1))
                    .stream().map(CoinCheckOpensOrdersResponse.Order::getId).toList();
            log.info("{} {} {} {} {}", value("kind", "order-v5"), value("trace-id", uuid),
                    value("action", "opens-orders"),
                    value("order-id", response.getId()), value("opens-ids", opensOrdersIds));
            if (!opensOrdersIds.contains(response.getId())) {
                orderTransactionService.remove(orderRequest.getGroup());
                log.info("{} {} {} {}", value("kind", "order-v5"), value("trace-id", uuid),
                        value("action", "completed"),
                        value("order-transaction", response));
                isNeedCancel.set(false);
                executors.shutdown();
            }
        }, 30, 30, TimeUnit.SECONDS);

        // 一定時間経過後キャンセル
        executors.schedule(() -> {
            if (isNeedCancel.get() && repository.exchangeCancel(response.getId())) {
                orderTransactionService.set(orderRequest.getGroup(), OrderTransaction.builder()
                        .orderId(response.getId())
                        .orderStatus(OrderStatus.CANCEL)
                        .createdAt(Objects.isNull(response.getCreatedAt())
                                ? DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.from(ZoneOffset.UTC)).format(clock.instant())
                                : response.getCreatedAt())
                        .orderType(orderRequest.isBuy() ? OrderType.BUY : OrderType.SELL)
                        .build());
                log.info("{} {} {} {}", value("kind", "order-v5"), value("trace-id", uuid),
                        value("action", "cancel"),
                        value("order-transaction", response));
            }
            executors.shutdown();
        }, apiConfig.getCancelDelayMinutes(), TimeUnit.MINUTES);
    }
}
