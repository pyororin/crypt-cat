package pyororin.cryptcat.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.config.CoinCheckRequestConfig;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckBalanceResponse;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;
import pyororin.cryptcat.repository.model.Pair;

import java.math.BigDecimal;

import static net.logstash.logback.argument.StructuredArguments.value;

@RequiredArgsConstructor
@Repository
@Slf4j
@ConditionalOnProperty(name = "coincheck.actually", havingValue = "false", matchIfMissing = true)
public class SkipCoinCheckRepositoryImpl implements CoinCheckRepository {
    private final RestClient restClient;

    @Override
    public CoinCheckTickerResponse retrieveTicker(CoinCheckRequest request) {
        return restClient.get()
                .uri("/api/ticker/?pair={rate}", request.getPair().getValue())
                .retrieve()
                .toEntity(CoinCheckTickerResponse.class).getBody();
    }

    @Override
    public void exchangeBuy(CoinCheckRequest request) {
        log.info("{} {} {} {} {} {} {}",
                value("kind", "exchange-skip"),
                value("pair", request.getPair().getValue()),
                value("order_type", request.getOrderType()),
                value("market_buy_amount", request.getAmount()),
                value("market_buy_price", request.getPrice()),
                value("order_rate", request.getRate()),
                value("group", request.getGroup()));
    }

    @Override
    public void exchangeSell(CoinCheckRequest request) {
        log.info("{} {} {} {} {} {} {}",
                value("kind", "exchange-skip"),
                value("pair", request.getPair().getValue()),
                value("order_type", request.getOrderType()),
                value("market_buy_amount", request.getAmount()),
                value("market_buy_price", request.getPrice()),
                value("order_rate", request.getRate()),
                value("group", request.getGroup()));
    }
}
