package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.TradeService;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@ConditionalOnProperty(name = "coincheck.actually", havingValue = "true")
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {
    private final CoinCheckRepository repository;
    private final CoinCheckApiConfig apiConfig;

    @Override
    public BigDecimal buy(Pair pair) {
        /* 市場最終価格(ticker.last) * 注文量(amount) = 注文価格 */
        var tickerResponse = repository.retrieveTicker(CoinCheckRequest.builder().pair(pair).build());
        var marketBuyAmount = BigDecimal.valueOf(tickerResponse.getLast()).multiply(apiConfig.getAmount());
        log.info("{} {} {} {} {}",
                value("kind", "exchange"),
                value("pair", "btc_jpy"),
                value("order_type", "market_buy"),
                value("market_buy_amount", marketBuyAmount),
                value("order_rate", tickerResponse.getLast()));
        repository.exchangeBuy(Pair.BTC_JPY, marketBuyAmount);
        return marketBuyAmount;
    }

    @Override
    public BigDecimal sell(Pair pair) {
        var tickerResponse = repository.retrieveTicker(CoinCheckRequest.builder().pair(pair).build());
        var marketSellAmount = BigDecimal.valueOf(tickerResponse.getLast()).divide(apiConfig.getAmount(), RoundingMode.HALF_EVEN);
        log.info("{} {} {} {} {}",
                value("kind", "exchange"),
                value("pair", "btc_jpy"),
                value("order_type", "market_sell"),
                value("market_sell_amount", marketSellAmount),
                value("order_rate", tickerResponse.getLast()));
        repository.exchangeSell(Pair.BTC_JPY, apiConfig.getAmount());
        return marketSellAmount;
    }
}