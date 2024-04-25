package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

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
                    .rate(tickerResponse.getFairBuyPrice())
                    .group(orderRequest.getGroup())
                    .build());
            return marketBuyPrice;
        } else {
            /* 市場最終価格(ticker.last or ticker.ask) = rate */
            /* 固定注文量 = amount */
            var marketSellPrice = tickerResponse.getFairSellPrice().multiply(apiConfig.getAmount());
            repository.exchangeSell(CoinCheckRequest.builder()
                    .pair(pair)
                    .price(marketSellPrice)
                    .amount(apiConfig.getAmount())
                    .rate(tickerResponse.getFairSellPrice())
                    .group(orderRequest.getGroup())
                    .build());
            return marketSellPrice;
        }
    }
}