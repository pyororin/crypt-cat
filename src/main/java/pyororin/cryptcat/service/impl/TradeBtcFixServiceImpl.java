package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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
@RequiredArgsConstructor
public class TradeBtcFixServiceImpl implements TradeService {
    private final CoinCheckRepository repository;
    private final CoinCheckApiConfig apiConfig;

    @Override
    public void order(Pair pair, OrderRequest orderRequest) {
        if (!orderRequest.isBuy() && !orderRequest.isSell()) {
            log.warn("BodyパラメータにorderTypeが無いか、buy|sell ではありません");
            return;
        }

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        // 5秒ごとにタスクを実行する
        LongStream.range(0, orderRequest.getRatio().longValue())
                .forEach(i -> executor.schedule(() -> exchange(pair, orderRequest), i * apiConfig.getInterval(), TimeUnit.SECONDS));

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

    @Retryable(retryFor = RestClientException.class, maxAttempts = 5, backoff = @Backoff(delay = 6000))
    private BigDecimal exchange(Pair pair, OrderRequest orderRequest) {
        var tickerResponse = repository.retrieveTicker(CoinCheckRequest.builder().pair(pair).build());
        if (orderRequest.isBuy()) {
            /* 市場最終価格(ticker.last or ticker.ask) = rate */
            /* 固定注文量 * アラート別レシオ = amount */
            var marketBuyPrice = tickerResponse.getFairBuyPrice().multiply(apiConfig.getAmount());
            repository.exchangeBuy(CoinCheckRequest.builder()
                    .pair(pair)
                    .price(marketBuyPrice)
                    .amount(apiConfig.getAmount())
                    .build());
            log.info("{} {} {} {} {} {} {}",
                    value("kind", "exchange"),
                    value("pair", "btc_jpy"),
                    value("order_type", "market_buy"),
                    value("market_buy_amount", apiConfig.getAmount()),
                    value("market_buy_price", marketBuyPrice),
                    value("order_rate", tickerResponse.getFairBuyPrice()),
                    value("group", orderRequest.getGroup()));
            return marketBuyPrice;
        } else {
            /* 市場最終価格(ticker.last or ticker.ask) = rate */
            /* 固定注文量 = amount */
            var marketSellPrice = tickerResponse.getFairSellPrice().multiply(apiConfig.getAmount());
            repository.exchangeSell(CoinCheckRequest.builder()
                    .pair(pair)
                    .price(marketSellPrice)
                    .amount(apiConfig.getAmount())
                    .build());
            log.info("{} {} {} {} {} {} {}",
                    value("kind", "exchange"),
                    value("pair", "btc_jpy"),
                    value("order_type", "market_sell"),
                    value("market_sell_amount", apiConfig.getAmount()),
                    value("market_sell_price", marketSellPrice),
                    value("order_rate", tickerResponse.getFairSellPrice()),
                    value("group", orderRequest.getGroup()));
            return marketSellPrice;
        }
    }

    @Recover
    public ResponseEntity<String> recover(Exception e) {
        log.error("{} {} {}", value("kind", "api"), value("cause", "APIリトライ回数超過"), value("message", e.getMessage()));
        return ResponseEntity.ok("リトライ後のリカバリー");
    }
}