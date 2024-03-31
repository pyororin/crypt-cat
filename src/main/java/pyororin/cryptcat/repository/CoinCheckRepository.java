package pyororin.cryptcat.repository;

import com.google.api.client.http.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import pyororin.cryptcat.config.CoinCheckRequestConfig;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.CoinCheckResponse;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;
import pyororin.cryptcat.repository.model.Pair;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class CoinCheckRepository {
    private final RestClient restClient;
    private final CoinCheckRequestConfig config;

    private static String HMAC_SHA256Encode(String secretKey, String message) {
        Mac mac = null;
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

    public CoinCheckTickerResponse retrieveTicker(CoinCheckRequest request) {
        return restClient.get()
                .uri("/api/ticker/?pair={rate}", request.getPair().getValue())
                .retrieve()
                .toEntity(CoinCheckTickerResponse.class).getBody();
    }

//    public CoinCheckResponse exchangeBuy(Pair pair, int amount) {
//
//    }

    public String getBalance() {
        String url = "https://coincheck.com/api/accounts/balance";
        return requestByUrlWithHeader(url, createHeader(url));
    }

    private Map<String, String> createHeader(String url) {
        Map<String, String> map = new HashMap<>();
        String nonce = String.valueOf(System.currentTimeMillis() / 1000L);;
        map.put("ACCESS-KEY", config.getAccessKey());
        map.put("ACCESS-NONCE", nonce);
        map.put("ACCESS-SIGNATURE", HMAC_SHA256Encode(config.getSecret(), nonce + url));
        return map;
    }



    private String requestByUrlWithHeader(String url, final Map<String, String> headers) {
//        var transport = new ApacheHttpTransport();
        HttpTransport transport = new NetHttpTransport();
        HttpRequestFactory factory = transport.createRequestFactory(request -> {
            request.setConnectTimeout(0);
            request.setReadTimeout(0);
            request.setParser(new GsonFactory().createJsonObjectParser());
            final HttpHeaders httpHeaders = new HttpHeaders();
            for (Map.Entry<String, String> e : headers.entrySet()) {
                httpHeaders.set(e.getKey(), e.getValue());
            }
            request.setHeaders(httpHeaders);
        });
        String jsonString;
        try {
            HttpRequest request = factory.buildGetRequest(new GenericUrl(url));
            HttpResponse response = request.execute();
            jsonString = response.parseAsString();
        } catch (IOException e) {
            jsonString = null;
        }
        return jsonString;
    }
}