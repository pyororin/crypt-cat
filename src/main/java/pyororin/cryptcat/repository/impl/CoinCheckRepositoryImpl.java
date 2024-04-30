package pyororin.cryptcat.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.config.CoinCheckRequestConfig;
import pyororin.cryptcat.repository.BeforeWait;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
@Repository
@Slf4j
@ConditionalOnProperty(name = "coincheck.actually", havingValue = "true")
public class CoinCheckRepositoryImpl implements CoinCheckRepository {
    private final RestClient restClient;
    private final CoinCheckRequestConfig config;
    private final CoinCheckApiConfig apiConfig;

    @Override
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    public CoinCheckTickerResponse retrieveTicker(CoinCheckRequest request) {
        return restClient.get()
                .uri("/api/ticker/?pair={rate}", request.getPair().getValue())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("{} {} {} {}",
                            value("kind", "api"), value("uri", req.getURI().getPath()), value("status", res.getStatusText()),
                            value("response", new Scanner(res.getBody()).useDelimiter("\\A").next().replaceAll("\\r\\n|\\r|\\n", "")));
                    throw new RestClientException(res.getStatusCode().toString());
                })
                .toEntity(CoinCheckTickerResponse.class).getBody();
    }

    @Override
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    @BeforeWait
    public CoinCheckBalanceResponse retrieveBalance() {
        String nonce = String.valueOf(System.currentTimeMillis() / 1000L);
        return restClient.get()
                .uri("/api/accounts/balance")
                .headers(httpHeaders -> {
                    httpHeaders.set("ACCESS-KEY", config.getAccessKey());
                    httpHeaders.set("ACCESS-NONCE", nonce);
                    httpHeaders.set("ACCESS-SIGNATURE", HMAC_SHA256Encode(config.getSecret(), nonce + apiConfig.getHost() + "/api/accounts/balance"));
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("{} {} {} {}",
                            value("kind", "api"), value("uri", req.getURI().getPath()), value("status", res.getStatusText()),
                            value("response", new Scanner(res.getBody()).useDelimiter("\\A").next().replaceAll("\\r\\n|\\r|\\n", "")));
                    throw new RestClientException(res.getStatusCode().toString());
                })
                .toEntity(CoinCheckBalanceResponse.class).getBody();
    }

    @Override
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    @BeforeWait
    public CoinCheckOpensOrdersResponse retrieveOpensOrders() {
        String nonce = String.valueOf(System.currentTimeMillis() / 1000L);
        return restClient.get()
                .uri("/api/exchange/orders/opens")
                .headers(httpHeaders -> {
                    httpHeaders.set("ACCESS-KEY", config.getAccessKey());
                    httpHeaders.set("ACCESS-NONCE", nonce);
                    httpHeaders.set("ACCESS-SIGNATURE", HMAC_SHA256Encode(config.getSecret(), nonce + apiConfig.getHost() + "/api/exchange/orders/opens"));
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("{} {} {} {}",
                            value("kind", "api"), value("uri", req.getURI().getPath()), value("status", res.getStatusText()),
                            value("response", new Scanner(res.getBody()).useDelimiter("\\A").next().replaceAll("\\r\\n|\\r|\\n", "")));
                    throw new RestClientException(res.getStatusCode().toString());
                })
                .toEntity(CoinCheckOpensOrdersResponse.class).getBody();
    }

    @Override
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    @BeforeWait
    public CoinCheckTransactionsResponse retrieveOrdersTransactions() {
        String nonce = String.valueOf(System.currentTimeMillis() / 1000L);
        return restClient.get()
                .uri("/api/exchange/orders/transactions_pagination?limit={limit}", 100)
                .headers(httpHeaders -> {
                    httpHeaders.set("ACCESS-KEY", config.getAccessKey());
                    httpHeaders.set("ACCESS-NONCE", nonce);
                    httpHeaders.set("ACCESS-SIGNATURE", HMAC_SHA256Encode(config.getSecret(), nonce + apiConfig.getHost()
                            + String.format("/api/exchange/orders/transactions_pagination?limit=%d", 100)));
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("{} {} {} {}",
                            value("kind", "api"), value("uri", req.getURI().getPath()), value("status", res.getStatusText()),
                            value("response", new Scanner(res.getBody()).useDelimiter("\\A").next().replaceAll("\\r\\n|\\r|\\n", "")));
                    throw new RestClientException(res.getStatusCode().toString());
                })
                .toEntity(CoinCheckTransactionsResponse.class).getBody();
    }

    @Override
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    @BeforeWait
    public CoinCheckResponse exchangeBuyLimit(CoinCheckRequest request) {
        var jsonBody = new JSONObject();
        jsonBody.put("pair", request.getPair().getValue());
        jsonBody.put("order_type", "buy");
        jsonBody.put("rate", request.getRate().longValue());
        jsonBody.put("amount", request.getAmount());
        var response = exchange(jsonBody);
        log.info("{} {} {} {} {} {} {}",
                value("kind", "exchange"),
                value("pair", request.getPair().getValue()),
                value("order_type", "buy"),
                value("market_buy_amount", request.getAmount()),
                value("market_buy_price", request.getPrice()),
                value("order_rate", request.getRate()),
                value("group", request.getGroup()));
        return response;
    }

    @Override
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    @BeforeWait
    public CoinCheckResponse exchangeSellLimit(CoinCheckRequest request) {
        var jsonBody = new JSONObject();
        jsonBody.put("pair", request.getPair().getValue());
        jsonBody.put("order_type", "sell");
        jsonBody.put("rate", request.getRate().longValue());
        jsonBody.put("amount", request.getAmount());
        var response = exchange(jsonBody);
        log.info("{} {} {} {} {} {} {}",
                value("kind", "exchange"),
                value("pair", request.getPair().getValue()),
                value("order_type", "sell"),
                value("market_sell_amount", request.getAmount()),
                value("market_sell_price", request.getPrice()),
                value("order_rate", request.getRate()),
                value("group", request.getGroup()));
        return response;
    }

    @Override
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    @BeforeWait
    public void exchangeBuyMarket(CoinCheckRequest request) {
        var jsonBody = new JSONObject();
        jsonBody.put("pair", request.getPair().getValue());
        jsonBody.put("order_type", "market_buy");
        jsonBody.put("market_buy_amount", request.getPrice());
        exchange(jsonBody);
        log.info("{} {} {} {} {} {} {}",
                value("kind", "exchange"),
                value("pair", request.getPair().getValue()),
                value("order_type", "buy"),
                value("market_buy_amount", request.getAmount()),
                value("market_buy_price", request.getPrice()),
                value("order_rate", request.getRate()),
                value("group", request.getGroup()));
    }

    @Override
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    @BeforeWait
    public void exchangeSellMarket(CoinCheckRequest request) {
        var jsonBody = new JSONObject();
        jsonBody.put("pair", request.getPair().getValue());
        jsonBody.put("order_type", "market_sell");
        jsonBody.put("amount", request.getAmount());
        exchange(jsonBody);
        log.info("{} {} {} {} {} {} {}",
                value("kind", "exchange"),
                value("pair", request.getPair().getValue()),
                value("order_type", "sell"),
                value("market_sell_amount", request.getAmount()),
                value("market_sell_price", request.getPrice()),
                value("order_rate", request.getRate()),
                value("group", request.getGroup()));
    }

    @Override
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 2, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    @BeforeWait
    public void exchangeCancel(long id) {
        String nonce = String.valueOf(System.currentTimeMillis() / 1000L);
        restClient.delete()
                .uri("/api/exchange/orders/{id}", id)
                .headers(httpHeaders -> {
                    httpHeaders.set("ACCESS-KEY", config.getAccessKey());
                    httpHeaders.set("ACCESS-NONCE", nonce);
                    httpHeaders.set("ACCESS-SIGNATURE", HMAC_SHA256Encode(config.getSecret(),
                            nonce + apiConfig.getHost() + "/api/exchange/orders/" + id));
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    var message = new Scanner(res.getBody()).useDelimiter("\\A").next().replaceAll("\\r\\n|\\r|\\n", "");
                    if (!message.contains("Failed to cancel the order.")) {
                        log.error("{} {} {} {} {}",
                                value("kind", "api"), value("uri", req.getURI().getPath()), value("status", res.getStatusText()), value("id", id),
                                value("response", message));
                    }
                    throw new RestClientException(res.getStatusCode().toString());
                })
                .toBodilessEntity();
        log.info("{} {} {} {}",
                value("kind", "api"), value("uri", "/api/exchange/orders/"), value("status", "ok"), value("id", id));
    }

    private CoinCheckResponse exchange(JSONObject jsonBody) {
        String nonce = String.valueOf(System.currentTimeMillis() / 1000L);
        var response = restClient.post()
                .uri("/api/exchange/orders")
                .contentType(APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.set("ACCESS-KEY", config.getAccessKey());
                    httpHeaders.set("ACCESS-NONCE", nonce);
                    httpHeaders.set("ACCESS-SIGNATURE", HMAC_SHA256Encode(config.getSecret(),
                            nonce + apiConfig.getHost() + "/api/exchange/orders" + jsonBody));
                })
                .body(jsonBody.toString())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("{} {} {} {} {}",
                            value("kind", "api"), value("uri", req.getURI().getPath()), value("status", res.getStatusText()), value("request-body", jsonBody.toString()),
                            value("response", new Scanner(res.getBody()).useDelimiter("\\A").next().replaceAll("\\r\\n|\\r|\\n", "")));
                    throw new RestClientException(res.getStatusCode().toString());
                })
                .toEntity(CoinCheckResponse.class).getBody();
        log.info("{} {} {} {}",
                value("kind", "api"), value("uri", "/api/exchange/orders"), value("status", "ok"), value("request-body", jsonBody.toString()));
        return response;
    }

    private static String HMAC_SHA256Encode(String secretKey, String message) {
        Mac mac;
        try {
            mac = Mac.getInstance("hmacSHA256");
            mac.init(new SecretKeySpec(
                    secretKey.getBytes(),
                    "hmacSHA256"));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return Hex.encodeHexString(mac.doFinal(message.getBytes()));
    }

    @Recover
    public CoinCheckTickerResponse tickerRecover(Exception e) {
        log.error("{} {} {}", value("kind", "api"), value("cause", "APIリトライ回数超過"), value("message", e.getMessage()));
        throw new RestClientException("APIリトライ回数超過", e);
    }

    @Recover
    public void exchangeRecover(Exception e) {
        log.error("{} {} {}", value("kind", "api"), value("cause", "APIリトライ回数超過"), value("message", e.getMessage()));
        throw new RestClientException("APIリトライ回数超過", e);
    }
}