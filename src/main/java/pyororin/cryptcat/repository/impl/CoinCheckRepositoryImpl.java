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
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckBalanceResponse;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;

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
                .toEntity(CoinCheckTickerResponse.class).getBody();
    }

    @Override
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, maxDelay = 5000))
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
                .toEntity(CoinCheckBalanceResponse.class).getBody();
    }

    @Retryable(retryFor = RuntimeException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    public void exchangeBuy(CoinCheckRequest request) {
        var jsonBody = new JSONObject();
        jsonBody.put("pair", request.getPair().getValue());
        jsonBody.put("order_type", "buy");
        jsonBody.put("rate", request.getRate().longValue());
        jsonBody.put("amount", request.getAmount());
        exchange(jsonBody);
    }

    @Retryable(retryFor = RuntimeException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    public void exchangeSell(CoinCheckRequest request) {
        var jsonBody = new JSONObject();
        jsonBody.put("pair", request.getPair().getValue());
        jsonBody.put("order_type", "sell");
        jsonBody.put("rate", request.getRate().longValue());
        jsonBody.put("amount", request.getAmount());
        exchange(jsonBody);
    }

    private void exchange(JSONObject jsonBody) {
        String nonce = String.valueOf(System.currentTimeMillis() / 1000L);
        restClient.post()
                .uri("/api/exchange/orders")
                .contentType(APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.set("ACCESS-KEY", config.getAccessKey());
                    httpHeaders.set("ACCESS-NONCE", nonce);
                    httpHeaders.set("ACCESS-SIGNATURE", HMAC_SHA256Encode(config.getSecret(), nonce + apiConfig.getHost() + "/api/exchange/orders" + jsonBody));
                })
                .body(jsonBody.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is2xxSuccessful, (req, res) -> log.info("{} {} {} {}",
                        value("kind", "api"), value("status", "ok"), value("request-body", jsonBody.toString()),
                        value("response", new Scanner(res.getBody()).useDelimiter("\\A").next().replaceAll("\\r\\n|\\r|\\n", ""))))
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("{} {} {} {}",
                            value("kind", "api"), value("status", res.getStatusText()), value("request-body", jsonBody.toString()),
                            value("response", new Scanner(res.getBody()).useDelimiter("\\A").next().replaceAll("\\r\\n|\\r|\\n", "")));
                    throw new RestClientException(res.getStatusCode().toString());
                })
                .toBodilessEntity();
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