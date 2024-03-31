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
@ConditionalOnProperty(name = "coincheck.actually", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
public class SkipTradeServiceImpl implements TradeService {
    private final CoinCheckRepository repository;
    private final CoinCheckApiConfig apiConfig;

    @Override
    public BigDecimal buy(Pair pair) {
        var tickerResponse = repository.retrieveTicker(CoinCheckRequest.builder().pair(pair).build());
        var marketBuyAmount = BigDecimal.valueOf(tickerResponse.getLast()).multiply(apiConfig.getAmount());
        // Skipするため現在の最終レートで確定とする
        var orderRate = BigDecimal.valueOf(tickerResponse.getLast()).divide(marketBuyAmount, RoundingMode.HALF_EVEN);
        log.info("{} {} {} {} {}",
                value("kind", "exchange-skip"),
                value("pair", "btc_jpy"),
                value("order_type", "market_buy"),
                value("market_buy_amount", marketBuyAmount),
                value("order_rate", orderRate));
        return marketBuyAmount;
    }

    @Override
    public BigDecimal sell(Pair pair) {
        var tickerResponse = repository.retrieveTicker(CoinCheckRequest.builder().pair(pair).build());
        var amount = apiConfig.getAmount();
        // Skipするため現在の最終レートで確定とする
        var orderRate = BigDecimal.valueOf(tickerResponse.getLast()).divide(amount, RoundingMode.HALF_EVEN);
        log.info("{} {} {} {} {}",
                value("kind", "exchange-skip"),
                value("pair", "btc_jpy"),
                value("order_type", "market_sell"),
                value("amount", amount),
                value("order_rate", orderRate));
        return amount;
    }
}
