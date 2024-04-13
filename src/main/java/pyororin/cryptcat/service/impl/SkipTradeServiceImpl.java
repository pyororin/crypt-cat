package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.TradeService;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import static net.logstash.logback.argument.StructuredArguments.value;


@Slf4j
@Service
@ConditionalOnProperty(name = "coincheck.actually", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
public class SkipTradeServiceImpl implements TradeService {
    private final CoinCheckRepository repository;
    private final CoinCheckApiConfig apiConfig;

    private BigDecimal buy(Pair pair, OrderRequest orderRequest) {
        /* 市場最終価格(ticker.last or ticker.ask) * 注文量(amount) = 注文価格 */
        var tickerResponse = repository.retrieveTicker(CoinCheckRequest.builder().pair(pair).build());
        var marketBuyAmount = tickerResponse.getFairBuyPrice()
                .multiply(apiConfig.getAmount()).multiply(orderRequest.getRatio());
        log.info("{} {} {} {} {} {}",
                value("kind", "exchange-skip"),
                value("pair", "btc_jpy"),
                value("order_type", "market_buy"),
                value("market_buy_amount", marketBuyAmount),
                value("order_rate", tickerResponse.getFairBuyPrice()),
                value("group", orderRequest.getGroup()));
        return marketBuyAmount;
    }

    private BigDecimal sell(Pair pair, OrderRequest orderRequest) {
        /* 市場最終価格(ticker.last or ticker.ask) * 注文量(amount) = 注文価格 */
        var tickerResponse = repository.retrieveTicker(CoinCheckRequest.builder().pair(pair).build());
        var marketSellAmount = tickerResponse.getFairSellPrice()
                .multiply(apiConfig.getAmount()).multiply(orderRequest.getRatio());
        log.info("{} {} {} {} {} {}",
                value("kind", "exchange-skip"),
                value("pair", "btc_jpy"),
                value("order_type", "market_sell"),
                value("market_sell_amount", marketSellAmount),
                value("order_rate", tickerResponse.getFairSellPrice()),
                value("group", orderRequest.getGroup()));
        return marketSellAmount;
    }

    @Override
    public BigDecimal order(Pair pair, OrderRequest orderRequest) {
        if (orderRequest.isBuy()) {
            return this.buy(pair, orderRequest);
        }
        if (orderRequest.isSell()) {
            return this.sell(pair, orderRequest);
        }
        log.warn("BodyパラメータにorderTypeが無いか、buy|sell ではありません");
        return BigDecimal.valueOf(0);
    }

    @Override
    public void orderSplit(Pair pair, OrderRequest orderRequest) {
        if (!orderRequest.isBuy() && !orderRequest.isSell()) {
            log.warn("BodyパラメータにorderTypeが無いか、buy|sell ではありません");
            return;
        }

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        // 5秒ごとにタスクを実行する
        LongStream.range(0, orderRequest.getRatio().longValue())
                .forEach(i -> executor.schedule(() -> {
                    if (orderRequest.isBuy()) {
                        buy(pair, orderRequest);
                    }
                    if (orderRequest.isSell()) {
                        sell(pair, orderRequest);
                    }
                }, i * apiConfig.getInterval(), TimeUnit.SECONDS));

        // すべてのタスクが完了したらシャットダウン
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
