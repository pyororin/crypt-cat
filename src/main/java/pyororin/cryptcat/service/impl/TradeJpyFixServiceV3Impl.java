package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckOpensOrdersResponse;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.TradeService;

import java.math.RoundingMode;
import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.LongStream;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeJpyFixServiceV3Impl implements TradeService {
    private final Clock clock;
    private final TradeRateLogicService tradeRateLogicService;
    private final CoinCheckRepository repository;
    private final CoinCheckApiConfig apiConfig;
    private final CoinCheckApiConfig.Retry retry;

    @Override
    public void order(Pair pair, OrderRequest orderRequest) {
        if (!orderRequest.isBuy() && !orderRequest.isSell()) {
            log.warn("BodyパラメータにorderTypeが無いか、buy|sell ではありません");
            return;
        }
        exchange(pair, orderRequest);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        // 指定した秒ごとにタスクを実行する
        LongStream.range(0, orderRequest.getRatio().longValue() - 1)
                .forEach(i -> executor.schedule(() -> exchange(pair, orderRequest), i * apiConfig.getInterval(), TimeUnit.SECONDS));
    }

    private void exchange(Pair pair, OrderRequest orderRequest) {
        var hop = new AtomicInteger(retry.getLimitCount());
        if (orderRequest.isBuy()) {
            var buyPrice = tradeRateLogicService.getFairBuyPrice(pair);
            /* 市場最終価格(ticker.last or ticker.ask) = rate */
            /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
            var amount = apiConfig.getPrice().divide(buyPrice, 9, RoundingMode.HALF_EVEN);
            var response = new AtomicReference<>(repository.exchangeBuyLimit(CoinCheckRequest.builder()
                    .pair(pair)
                    .price(apiConfig.getPrice())
                    .amount(amount)
                    .rate(buyPrice)
                    .group(orderRequest.getGroup())
                    .build()));

            // 一定回数指値リトライ
            var executors = Executors.newScheduledThreadPool(1);
            executors.scheduleWithFixedDelay(() -> {
                var opensOrdersIds = repository.retrieveOpensOrders().findOrdersWithinMinuets(clock, 0, retry.getDelayMin() * 2)
                        .stream().map(CoinCheckOpensOrdersResponse.Order::getId).toList();
                log.info("{} {} {}", value("kind", "limit-retry"), value("order-id", response.get().getId()), value("opens-ids", opensOrdersIds));
                if (hop.get() > 0 && opensOrdersIds.contains(response.get().getId())) {
                    repository.exchangeCancel(response.get().getId());
                    var buyPriceRetry = tradeRateLogicService.getFairBuyPrice(pair);
                    /* 市場最終価格(ticker.last or ticker.ask) = rate */
                    /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
                    response.set(repository.exchangeBuyLimit(CoinCheckRequest.builder()
                            .pair(pair)
                            .price(apiConfig.getPrice())
                            .amount(apiConfig.getPrice().divide(buyPriceRetry, 9, RoundingMode.HALF_EVEN))
                            .rate(buyPriceRetry)
                            .group("limit-retry")
                            .build()));
                    hop.getAndDecrement();
                } else {
                    executors.shutdown();
                }
            }, retry.getDelaySec(), retry.getDelaySec(), TimeUnit.SECONDS);

            try {
                if (!executors.awaitTermination(retry.getDelaySec() * (retry.getLimitCount() + 1), TimeUnit.SECONDS)) {
                    executors.shutdown();
                }
            } catch(InterruptedException ie) {
                executors.shutdown();
            }

            // 成行リトライ
            Executors.newScheduledThreadPool(1).schedule(() -> {
                var opensOrdersIds = repository.retrieveOpensOrders().findOrdersWithinMinuets(clock, 0, retry.getDelayMin() * 2)
                        .stream().map(CoinCheckOpensOrdersResponse.Order::getId).toList();
                log.info("{} {} {}", value("kind", "market-retry"), value("order-id", response.get().getId()), value("opens-ids", opensOrdersIds));
                if (opensOrdersIds.contains(response.get().getId())) {
                    // 本来の価格差分算出
                    // 指値amount - 成行amount
                    var marketAmount = apiConfig.getPrice().divide(
                            repository.retrieveTicker(CoinCheckRequest.builder().pair(pair).build()).getAsk(),
                            9, RoundingMode.HALF_EVEN);
                    log.info("{} {} {} {}", value("kind", "retry-buy-diff"),
                            value("market-amount", marketAmount), value("limit-amount", amount), value("diff-amount", marketAmount.subtract(amount)));
                    repository.exchangeCancel(response.get().getId());
                    repository.exchangeBuyMarket(CoinCheckRequest.builder()
                            .pair(pair)
                            .price(apiConfig.getPrice())
                            .group("market-retry")
                            .build());
                }

            }, retry.getDelaySec(), TimeUnit.SECONDS);
        } else {
            var sellPrice = tradeRateLogicService.getFairSellPrice(pair);
            /* 市場最終価格(ticker.last or ticker.ask) = rate */
            /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
            var amount = apiConfig.getPrice().divide(sellPrice, 9, RoundingMode.HALF_EVEN);
            var response = new AtomicReference<>(repository.exchangeSellLimit(CoinCheckRequest.builder()
                    .pair(pair)
                    .price(apiConfig.getPrice())
                    .amount(amount)
                    .rate(sellPrice)
                    .group(orderRequest.getGroup())
                    .build()));

            // 一定回数指値リトライ
            var executors = Executors.newScheduledThreadPool(1);
            executors.scheduleWithFixedDelay(() -> {
                var opensOrdersIds = repository.retrieveOpensOrders().findOrdersWithinMinuets(clock, 0, retry.getDelayMin() * 2)
                        .stream().map(CoinCheckOpensOrdersResponse.Order::getId).toList();
                log.info("{} {} {}", value("kind", "limit-retry"), value("order-id", response.get().getId()), value("opens-ids", opensOrdersIds));
                if (hop.get() > 0 && opensOrdersIds.contains(response.get().getId())) {
                    repository.exchangeCancel(response.get().getId());
                    var sellPriceRetry = tradeRateLogicService.getFairSellPrice(pair);
                    /* 市場最終価格(ticker.last or ticker.ask) = rate */
                    /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
                    response.set(repository.exchangeSellLimit(CoinCheckRequest.builder()
                            .pair(pair)
                            .price(apiConfig.getPrice())
                            .amount(apiConfig.getPrice().divide(sellPriceRetry, 9, RoundingMode.HALF_EVEN))
                            .rate(sellPriceRetry)
                            .group("limit-retry")
                            .build()));
                    hop.getAndDecrement();
                } else {
                    executors.shutdown();
                }
            }, retry.getDelaySec(), retry.getDelaySec(), TimeUnit.SECONDS);

            try {
                if (!executors.awaitTermination(retry.getDelaySec() * (retry.getLimitCount() + 1), TimeUnit.SECONDS)) {
                    executors.shutdown();
                }
            } catch(InterruptedException ie) {
                executors.shutdown();
            }

            // 成行リトライ
            Executors.newScheduledThreadPool(1).schedule(() -> {
                var opensOrdersIds = repository.retrieveOpensOrders().findOrdersWithinMinuets(clock, 0, retry.getDelayMin() * 2)
                        .stream().map(CoinCheckOpensOrdersResponse.Order::getId).toList();
                log.info("{} {} {}", value("kind", "market-retry"), value("order-id", response.get().getId()), value("opens-ids", opensOrdersIds));
                if (opensOrdersIds.contains(response.get().getId())) {
                    // 本来の価格差分算出
                    // 指値amount - 成行amount
                    var marketAmount = apiConfig.getPrice().divide(
                            repository.retrieveTicker(CoinCheckRequest.builder().pair(pair).build()).getBid(),
                            9, RoundingMode.HALF_EVEN);
                    log.info("{} {} {} {}", value("kind", "retry-sell-diff"),
                            value("limit-amount", amount), value("market-amount", marketAmount), value("diff-amount", amount.subtract(marketAmount)));
                    repository.exchangeCancel(response.get().getId());
                    repository.exchangeSellMarket(CoinCheckRequest.builder()
                            .pair(pair)
                            .amount(apiConfig.getPrice().divide(tradeRateLogicService.getFairSellPrice(pair), 9, RoundingMode.HALF_EVEN))
                            .group("market-retry")
                            .build());
                }
            }, retry.getDelaySec(), TimeUnit.SECONDS);
        }
    }
}
