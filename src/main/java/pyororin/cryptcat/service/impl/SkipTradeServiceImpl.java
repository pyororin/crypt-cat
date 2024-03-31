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
        log.info("{} {} {} {}",
                value("kind", "exchange-skip"),
                value("pair", "btc_jpy"),
                value("order_type", "market_buy"),
                value("market_buy_amount", marketBuyAmount));
        return marketBuyAmount;
    }

    @Override
    public BigDecimal sell(Pair pair) {
        var amount = apiConfig.getAmount();
        log.info("{} {} {} {}",
                value("kind", "exchange-skip"),
                value("pair", "btc_jpy"),
                value("order_type", "market_sell"),
                value("amount", amount));
        return amount;
    }
}
