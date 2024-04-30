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

        // 5秒ごとにタスクを実行する
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

    private BigDecimal exchange(Pair pair, OrderRequest orderRequest) {
        if (orderRequest.isBuy()) {
            var buyPrice = tradeRateLogicService.getFairBuyPrice(pair);
            /* 市場最終価格(ticker.last or ticker.ask) = rate */
            /* 固定注文量 * アラート別レシオ = amount */
            var marketBuyPrice = buyPrice.multiply(apiConfig.getAmount());
            repository.exchangeBuyLimit(CoinCheckRequest.builder()
                    .pair(pair)
                    .price(marketBuyPrice)
                    .amount(apiConfig.getAmount())
                    .rate(buyPrice)
                    .group(orderRequest.getGroup())
                    .build());
            return marketBuyPrice;
        } else {
            var sellPrice = tradeRateLogicService.getFairSellPrice(pair);
            /* 市場最終価格(ticker.last or ticker.ask) = rate */
            /* 固定注文量 = amount */
            var marketSellPrice = sellPrice.multiply(apiConfig.getAmount());
            repository.exchangeSellLimit(CoinCheckRequest.builder()
                    .pair(pair)
                    .price(marketSellPrice)
                    .amount(apiConfig.getAmount())
                    .rate(sellPrice)
                    .group(orderRequest.getGroup())
                    .build());
            return marketSellPrice;
        }
    }
}