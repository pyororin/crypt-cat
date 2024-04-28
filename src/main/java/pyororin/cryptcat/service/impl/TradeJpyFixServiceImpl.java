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
import java.math.RoundingMode;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeJpyFixServiceImpl implements TradeService {
    private final TradeRateLogicService tradeRateLogicService;
    private final CoinCheckRepository repository;
    private final CoinCheckApiConfig apiConfig;

    @Override
    public void order(Pair pair, OrderRequest orderRequest) {
        if (!orderRequest.isBuy() && !orderRequest.isSell()) {
            log.warn("BodyパラメータにorderTypeが無いか、buy|sell ではありません");
            return;
        }

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        // 指定した秒ごとにタスクを実行する
        LongStream.range(0, orderRequest.getRatio().longValue())
                .forEach(i -> executor.schedule(() -> exchange(pair, orderRequest), i * apiConfig.getInterval(), TimeUnit.SECONDS));
        try {
            if (!executor.awaitTermination((orderRequest.getRatio().longValue() + 1) * apiConfig.getInterval(), TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Retryable(retryFor = RestClientException.class, maxAttempts = 5, backoff = @Backoff(delay = 6000))
    private BigDecimal exchange(Pair pair, OrderRequest orderRequest) {
        if (orderRequest.isBuy()) {
            var buyPrice = tradeRateLogicService.getFairBuyPrice(pair);
            /* 市場最終価格(ticker.last or ticker.ask) = rate */
            /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
            var amount = apiConfig.getPrice().divide(buyPrice, 9, RoundingMode.HALF_EVEN);
            repository.exchangeBuy(CoinCheckRequest.builder()
                    .pair(pair)
                    .price(apiConfig.getPrice())
                    .amount(amount)
                    .rate(buyPrice)
                    .group(orderRequest.getGroup())
                    .build());
            return amount;
        } else {
            var sellPrice = tradeRateLogicService.getFairSellPrice(pair);
            /* 市場最終価格(ticker.last or ticker.ask) = rate */
            /* 固定金額(JPY) / 市場最終価格(ticker.last or ticker.ask) = amount */
            var amount = apiConfig.getPrice().divide(sellPrice, 9, RoundingMode.HALF_EVEN);
            repository.exchangeSell(CoinCheckRequest.builder()
                    .pair(pair)
                    .price(apiConfig.getPrice())
                    .amount(amount)
                    .rate(sellPrice)
                    .group(orderRequest.getGroup())
                    .build());
            return amount;
        }
    }

    @Recover
    public ResponseEntity<String> recover(Exception e) {
        log.error("{} {} {}", value("kind", "api"), value("cause", "APIリトライ回数超過"), value("message", e.getMessage()));
        return ResponseEntity.ok("リトライ後のリカバリー");
    }
}
