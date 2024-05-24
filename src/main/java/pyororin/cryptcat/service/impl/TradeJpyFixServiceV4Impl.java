package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.config.OrderStatus;
import pyororin.cryptcat.config.OrderTransaction;
import pyororin.cryptcat.config.OrderTransactions;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.*;
import pyororin.cryptcat.service.TradeService;

import java.math.RoundingMode;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeJpyFixServiceV4Impl implements TradeService {
    private final Clock clock;
    private final TradeRateLogicService tradeRateLogicService;
    private final CoinCheckRepository repository;
    private final CoinCheckApiConfig apiConfig;
    private final OrderTransactions orderTransactions;

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
        CoinCheckResponse response;
        if (orderRequest.isBuy()) {
            if (orderTransactions.get(orderRequest.getGroup()).isBuySkip()) {
                log.info("{} {} {} {}", value("kind", "order-v4"), value("trace-id", uuid),
                        value("action", "skip-buy"),
                        value("order-transaction", orderTransactions.get(orderRequest.getGroup())));
                return;
            } else {
                var buyPrice = tradeRateLogicService.getFairBuyPrice(pair);
                /* 市場最終価格(ticker.last or ticker.ask) = rate */
                /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
                var amount = apiConfig.getPrice().multiply(orderRequest.getRatio()).divide(buyPrice, 9, RoundingMode.HALF_EVEN);
                response = repository.exchangeBuyLimit(CoinCheckRequest.builder()
                        .pair(pair)
                        .price(apiConfig.getPrice().multiply(orderRequest.getRatio()))
                        .amount(amount)
                        .rate(buyPrice)
                        .group(orderRequest.getGroup())
                        .build());
                orderTransactions.put(orderRequest.getGroup(), OrderTransaction.builder()
                        .orderId(response.getId())
                        .orderStatus(OrderStatus.ORDERED)
                        .createdAt(response.getCreatedAt())
                        .orderType(OrderType.BUY)
                        .build());
            }
        } else {
            if (orderTransactions.get(orderRequest.getGroup()).isSellSkip()) {
                log.info("{} {} {} {}", value("kind", "order-v4"), value("trace-id", uuid),
                        value("action", "skip-sell"),
                        value("order-transaction", orderTransactions.get(orderRequest.getGroup())));
                return;
            } else {
                var sellPrice = tradeRateLogicService.getFairSellPrice(pair);
                /* 市場最終価格(ticker.last or ticker.ask) = rate */
                /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
                var amount = apiConfig.getPrice().multiply(orderRequest.getRatio()).divide(sellPrice, 9, RoundingMode.HALF_EVEN);
                response = repository.exchangeSellLimit(CoinCheckRequest.builder()
                        .pair(pair)
                        .price(apiConfig.getPrice().multiply(orderRequest.getRatio()))
                        .amount(amount)
                        .rate(sellPrice)
                        .group(orderRequest.getGroup())
                        .build());
                orderTransactions.put(orderRequest.getGroup(), OrderTransaction.builder()
                        .orderId(response.getId())
                        .orderStatus(OrderStatus.ORDERED)
                        .createdAt(response.getCreatedAt())
                        .orderType(OrderType.SELL)
                        .build());
            }
        }

        // 一定時間注文確認
        var executors = Executors.newScheduledThreadPool(1);
        var isNeedCancel = new AtomicBoolean(true);
        executors.scheduleWithFixedDelay(() -> {
            var opensOrdersIds = repository.retrieveOpensOrders().findOrdersWithinMinuets(clock, 0, 10)
                    .stream().map(CoinCheckOpensOrdersResponse.Order::getId).toList();
            log.info("{} {} {} {} {}", value("kind", "order-v4"), value("trace-id", uuid),
                    value("action", "opens-orders"),
                    value("order-id", response.getId()), value("opens-ids", opensOrdersIds));
            if (!opensOrdersIds.contains(response.getId())) {
                isNeedCancel.set(false);
                executors.shutdown();
                orderTransactions.remove(orderRequest.getGroup());
                log.info("{} {} {} {}", value("kind", "order-v4"), value("trace-id", uuid),
                        value("action", "completed"),
                        value("order-transaction", response));
            }
        }, 30, 30, TimeUnit.SECONDS);

        // 一定時間経過後キャンセル
        executors.schedule(() -> {
            if (isNeedCancel.get() && repository.exchangeCancel(response.getId())) {
                orderTransactions.get(orderRequest.getGroup()).setOrderStatus(OrderStatus.CANCEL);
                log.info("{} {} {} {}", value("kind", "order-v4"), value("trace-id", uuid),
                        value("action", "cancel"),
                        value("order-transaction", response));
            }
            executors.shutdown();
        }, 600, TimeUnit.SECONDS);
    }
}
