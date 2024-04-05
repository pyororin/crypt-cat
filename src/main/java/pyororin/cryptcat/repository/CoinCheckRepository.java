package pyororin.cryptcat.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.config.CoinCheckRequestConfig;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.CoinCheckResponse;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;
import pyororin.cryptcat.repository.model.Pair;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
@Repository
@Slf4j
public class CoinCheckRepository {
    private final RestClient restClient;
    private final CoinCheckRequestConfig config;
    private final CoinCheckApiConfig apiConfig;

    public CoinCheckTickerResponse retrieveTicker(CoinCheckRequest request) {
        return restClient.get()
                .uri("/api/ticker/?pair={rate}", request.getPair().getValue())
                .retrieve()
                .toEntity(CoinCheckTickerResponse.class).getBody();
    }

    public void exchangeBuy(Pair pair, BigDecimal marketBuyAmount) {
        var jsonBody = new JSONObject();
        jsonBody.put("pair", pair.getValue());
        jsonBody.put("order_type", "market_buy");
        jsonBody.put("market_buy_amount", marketBuyAmount.toString());
        exchange(jsonBody);
    }

    public void exchangeSell(Pair pair, BigDecimal amount) {
        var jsonBody = new JSONObject();
        jsonBody.put("pair", pair.getValue());
        jsonBody.put("order_type", "market_sell");
        jsonBody.put("amount", amount.toString());
        exchange(jsonBody);
    }

    public String getBalance() {
        String nonce = String.valueOf(System.currentTimeMillis() / 1000L);
        return restClient.get()
                .uri("/api/accounts/balance")
                .headers(httpHeaders -> {
                    httpHeaders.set("ACCESS-KEY", config.getAccessKey());
                    httpHeaders.set("ACCESS-NONCE", nonce);
                    httpHeaders.set("ACCESS-SIGNATURE", HMAC_SHA256Encode(config.getSecret(), nonce + apiConfig.getHost() + "/api/accounts/balance"));
                })
                .retrieve()
                .toEntity(String.class).getBody();
    }

    private void exchange(JSONObject jsonBody) {
        String nonce = String.valueOf(System.currentTimeMillis() / 1000L);
        var response = restClient.post()
                .uri("/api/exchange/orders")
                .contentType(APPLICATION_JSON)
                .headers(httpHeaders -> {
                    httpHeaders.set("ACCESS-KEY", config.getAccessKey());
                    httpHeaders.set("ACCESS-KEY", config.getAccessKey());
                    httpHeaders.set("ACCESS-NONCE", nonce);
                    httpHeaders.set("ACCESS-SIGNATURE", HMAC_SHA256Encode(config.getSecret(), nonce + apiConfig.getHost() + "/api/exchange/orders" + jsonBody));
                })
                .body(jsonBody.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is2xxSuccessful, (req, res) -> log.info("{} {}", value("kind", "api"), value("status", "ok")))
                .onStatus(HttpStatusCode::isError, (req, res) -> log.error("{} {}", value("kind", "api"), value("status", res.getStatusText())))
                .toEntity(CoinCheckResponse.class).getBody();
        if (Boolean.parseBoolean(Objects.requireNonNull(response).getSuccess())) {
            log.info("{} {} {} {} {}",
                    value("kind", "response"),
                    value("pair", Objects.requireNonNull(response).getPair()),
                    value("order_type", response.getOrderType()),
                    value("market_amount", (new BigDecimal(response.getAmount())).multiply(new BigDecimal(response.getRate()))),
                    value("order_rate", response.getRate()));
        }
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
}