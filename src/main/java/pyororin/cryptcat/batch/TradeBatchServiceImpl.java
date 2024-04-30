package pyororin.cryptcat.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.impl.TradeRateLogicService;

import java.math.RoundingMode;
import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "coincheck.actually", havingValue = "true")
public class TradeBatchServiceImpl {
    private final Clock clock;
    private final CoinCheckApiConfig apiConfig;
    private final CoinCheckApiConfig.Retry retry;
    private final TradeRateLogicService tradeRateLogicService;
    private final CoinCheckRepository repository;

    @Scheduled(cron = "0 0 * * * *")
    public void balance() {
        var ticker = repository.retrieveTicker(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build());
        var balance = repository.retrieveBalance();
        var btcToJpy = balance.getBtc().multiply(ticker.getLast());
        var btcReservedToJpy = balance.getBtc_reserved().multiply(ticker.getLast());
        var jpyToBtc = balance.getJpy().divide(ticker.getLast(), 9, RoundingMode.HALF_EVEN);
        var jpyReservedToBtc = balance.getJpy_reserved().divide(ticker.getLast(), 9, RoundingMode.HALF_EVEN);
        log.info("{} {} {} {} {} {} {} {}",
                value("kind", "balance"),
                value("jpy", balance.getJpy()),
                value("jpy_reserved", balance.getJpy_reserved()),
                value("btc", balance.getBtc()),
                value("btc_reserved", balance.getBtc_reserved()),
                value("rate", ticker.getLast()),
                value("total_jpy", balance.getJpy().add(balance.getJpy_reserved()).add(btcToJpy).add(btcReservedToJpy).setScale(0, RoundingMode.HALF_EVEN)),
                value("total_btc", balance.getBtc().add(balance.getBtc_reserved()).add(jpyToBtc).add(jpyReservedToBtc).setScale(9, RoundingMode.HALF_EVEN))
        );
    }

    @Scheduled(cron = "0 1 * * * *")
    public void cancel() {
        var opensOrders = repository.retrieveOpensOrders();
        log.info("{} {}", value("kind", "cancel-batch"), value("count", opensOrders.getOrders().size()));
        opensOrders.findOrdersOver24Hours(clock).forEach(order -> {
            var executorService = Executors.newScheduledThreadPool(1);
            executorService.schedule(() -> {
                repository.exchangeCancel(order.getId());
                log.info("{} {} {} {} {} {} {} {}",
                        value("kind", "cancel"),
                        value("pair", order.getPair()),
                        value("pending_amount", order.getPendingAmount()),
                        value("pending_market_buy_amount", order.getPendingMarketBuyAmount()),
                        value("order_rate", order.getOrderType()),
                        value("order_time", order.getCreatedAt()),
                        value("stop_loss_rate", order.getStopLossRate()),
                        value("id", order.getId()));
            }, apiConfig.getInterval(), TimeUnit.SECONDS);
            executorService.shutdown();
        });
    }

    @Scheduled(cron = "30 */${coincheck.retry.interval-min} * * * *")
    public void cancelRetry() {
        if (apiConfig.isOrderRetry()) {
            var opensOrders = repository.retrieveOpensOrders().findOrdersWithinMinuets(
                    clock, retry.getDelayMin(), retry.getDelayMin() + (retry.getIntervalMin() * 3));
            log.info("{} {}", value("kind", "cancel-retry-batch"), value("count", opensOrders.size()));
            opensOrders.forEach(order -> {
                repository.exchangeCancel(order.getId());
                log.info("{} {} {} {} {} {} {} {}",
                        value("kind", "cancel"),
                        value("pair", order.getPair()),
                        value("pending_amount", order.getPendingAmount()),
                        value("pending_market_buy_amount", order.getPendingMarketBuyAmount()),
                        value("order_rate", order.getOrderType()),
                        value("order_time", order.getCreatedAt()),
                        value("stop_loss_rate", order.getStopLossRate()),
                        value("id", order.getId()));
                if (order.getOrderType().equals("sell")) {
                    var sellPrice = tradeRateLogicService.getFairSellPrice(Pair.fromValue(order.getPair()));
                    /* 市場最終価格(ticker.last or ticker.ask) = rate */
                    /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
                    var amount = apiConfig.getPrice().divide(sellPrice, 9, RoundingMode.HALF_EVEN);
                    repository.exchangeSellLimit(CoinCheckRequest.builder()
                            .pair(Pair.fromValue(order.getPair()))
                            .price(apiConfig.getPrice())
                            .amount(amount)
                            .rate(sellPrice)
                            .group("Cancel-Retry")
                            .build());
                }
                if (order.getOrderType().equals("buy")) {
                    var buyPrice = tradeRateLogicService.getFairBuyPrice(Pair.fromValue(order.getPair()));
                    /* 市場最終価格(ticker.last or ticker.ask) = rate */
                    /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
                    var amount = apiConfig.getPrice().divide(buyPrice, 9, RoundingMode.HALF_EVEN);
                    repository.exchangeBuyLimit(CoinCheckRequest.builder()
                            .pair(Pair.fromValue(order.getPair()))
                            .price(apiConfig.getPrice())
                            .amount(amount)
                            .rate(buyPrice)
                            .group("Cancel-Retry")
                            .build());
                }
            });
        }
    }

    @Scheduled(cron = "15 */10 * * * *")
    public void orders() {
        var ticker = repository.retrieveTicker(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build());
        var response = repository.retrieveOrdersTransactions();
        var sumFunds = response.sumFunds(clock, 10);
        var jpyToBtc = sumFunds.getJpy().divide(ticker.getLast(), 9, RoundingMode.HALF_EVEN);
        var btcToJpy = sumFunds.getBtc().multiply(ticker.getLast());
        log.info("{} {} {} {} {} {} {} {} {}",
                value("kind", "transactions"),
                value("count", response.findOrdersWithinMinutes(clock, 10).size()),
                value("sell-count", response.findOrdersWithinMinutes(clock, 10, "sell").size()),
                value("buy-count", response.findOrdersWithinMinutes(clock, 10, "buy").size()),
                value("jpy", sumFunds.getJpy()),
                value("btc", sumFunds.getBtc()),
                value("rate", ticker.getLast()),
                value("fix_jpy_btc", sumFunds.getBtc().add(jpyToBtc)),
                value("fix_btc_jpy", sumFunds.getJpy().add(btcToJpy))
        );
    }
}
