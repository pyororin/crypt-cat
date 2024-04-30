package pyororin.cryptcat.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.*;

import static net.logstash.logback.argument.StructuredArguments.value;

@RequiredArgsConstructor
@Repository
@Slf4j
@ConditionalOnProperty(name = "coincheck.actually", havingValue = "false", matchIfMissing = true)
public class SkipCoinCheckRepositoryImpl implements CoinCheckRepository {
    private final RestClient restClient;

    @Override
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    public CoinCheckTickerResponse retrieveTicker(CoinCheckRequest request) {
        return restClient.get()
                .uri("/api/ticker/?pair={rate}", request.getPair().getValue())
                .retrieve()
                .toEntity(CoinCheckTickerResponse.class).getBody();
    }

    @Override
    public CoinCheckBalanceResponse retrieveBalance() {
        return CoinCheckBalanceResponse.builder().build();
    }

    @Override
    public CoinCheckOpensOrdersResponse retrieveOpensOrders() {
        return CoinCheckOpensOrdersResponse.builder().build();
    }

    @Override
    public CoinCheckTransactionsResponse retrieveOrdersTransactions() {
        return CoinCheckTransactionsResponse.builder().build();
    }

    @Override
    public CoinCheckResponse exchangeBuyLimit(CoinCheckRequest request) {
        log.info("{} {} {} {} {} {} {}",
                value("kind", "exchange-skip"),
                value("pair", request.getPair().getValue()),
                value("order_type", "buy"),
                value("market_buy_amount", request.getAmount()),
                value("market_buy_price", request.getPrice()),
                value("order_rate", request.getRate()),
                value("group", request.getGroup()));
        return CoinCheckResponse.builder().build();
    }

    @Override
    public CoinCheckResponse exchangeSellLimit(CoinCheckRequest request) {
        log.info("{} {} {} {} {} {} {}",
                value("kind", "exchange-skip"),
                value("pair", request.getPair().getValue()),
                value("order_type", "sell"),
                value("market_sell_amount", request.getAmount()),
                value("market_sell_price", request.getPrice()),
                value("order_rate", request.getRate()),
                value("group", request.getGroup()));
        return CoinCheckResponse.builder().build();
    }

    @Override
    public void exchangeBuyMarket(CoinCheckRequest request) {
        log.info("{} {} {} {} {} {} {}",
                value("kind", "exchange-skip"),
                value("pair", request.getPair().getValue()),
                value("order_type", "market_buy"),
                value("market_buy_amount", request.getAmount()),
                value("market_buy_price", request.getPrice()),
                value("order_rate", request.getRate()),
                value("group", request.getGroup()));
    }

    @Override
    public void exchangeSellMarket(CoinCheckRequest request) {
        log.info("{} {} {} {} {} {} {}",
                value("kind", "exchange-skip"),
                value("pair", request.getPair().getValue()),
                value("order_type", "market_sell"),
                value("market_sell_amount", request.getAmount()),
                value("market_sell_price", request.getPrice()),
                value("order_rate", request.getRate()),
                value("group", request.getGroup()));
    }

    @Override
    public void exchangeCancel(long id) {
        log.info("{} {} {}",
                value("kind", "cancel-skip"), value("status", "ok"), value("id", id));
    }

    @Recover
    public CoinCheckTickerResponse tickerRecover(Exception e) {
        log.error("{} {} {}", value("kind", "api"), value("cause", "APIリトライ回数超過"), value("message", e.getMessage()));
        throw new RestClientException("APIリトライ回数超過", e);
    }
}
