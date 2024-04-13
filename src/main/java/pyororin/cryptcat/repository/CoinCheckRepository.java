package pyororin.cryptcat.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.config.CoinCheckRequestConfig;
import pyororin.cryptcat.repository.model.CoinCheckBalanceResponse;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;
import pyororin.cryptcat.repository.model.Pair;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

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

    public void exchangeBuy(Pair pair, BigDecimal rate, BigDecimal amount) {
        var jsonBody = new JSONObject();
        jsonBody.put("pair", pair.getValue());
        jsonBody.put("order_type", "buy");
        jsonBody.put("rate", rate);
        jsonBody.put("amount", amount);
        exchange(jsonBody);
    }

    public void exchangeSell(Pair pair, BigDecimal rate, BigDecimal amount) {
        var jsonBody = new JSONObject();
        jsonBody.put("pair", pair.getValue());
        jsonBody.put("order_type", "sell");
        jsonBody.put("rate", rate);
        jsonBody.put("amount", amount);
        exchange(jsonBody);
    }

    public CoinCheckBalanceResponse getBalance() {
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
                .onStatus(HttpStatusCode::is2xxSuccessful, (req, res) -> log.info("{} {} {}", value("kind", "api"), value("status", "ok"), value("response", new Scanner(res.getBody()).useDelimiter("\\A").next().replaceAll("\\r\\n|\\r|\\n", ""))))
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("{} {} {}", value("kind", "api"), value("status", res.getStatusText()), value("body", jsonBody.toString()));
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
}