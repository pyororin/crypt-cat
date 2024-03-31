package pyororin.cryptcat.repository;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
@Repository
public class CoinCheckRepository {
    private final RestClient restClient;

    public static String HMAC_SHA256Encode(String secretKey, String message) {

        SecretKeySpec keySpec = new SecretKeySpec(
                secretKey.getBytes(),
                "hmacSHA256");

        Mac mac = null;
        try {
            mac = Mac.getInstance("hmacSHA256");
            mac.init(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // can't recover
            throw new RuntimeException(e);
        }
        byte[] rawHmac = mac.doFinal(message.getBytes());
        return Hex.encodeHexString(rawHmac);
    }

    public CoinCheckTickerResponse retrieveTicker(CoinCheckRequest request) {
        return restClient.get()
                .uri("/api/ticker/pair={rate}", request.getPair().getValue())
                .retrieve()
                .toEntity(CoinCheckTickerResponse.class).getBody();
    }

//    public CoinCheckResponse exchangeBuy(Pair pair, int amount) {
//
//    }
//
//    private Map<String, String> createHeader(String url) {
//        Map<String, String> map = new HashMap<String, String>();
//        String nonce = createNonce();
//        map.put("ACCESS-KEY", apiKey);
//        map.put("ACCESS-NONCE", nonce);
//        map.put("ACCESS-SIGNATURE", createSignature(apiSecret, url, nonce));
//        return map;
//    }
//
//    private String createSignature(String apiSecret, String url, String nonce) {
//        String message = nonce + url;
//        return HMAC_SHA256Encode(apiSecret, message);
//    }
//
//    private String createNonce() {
//        long currentUnixTime = System.currentTimeMillis() / 1000L;
//        String nonce = String.valueOf(currentUnixTime);
//        return nonce;
//    }
//
//    private String requestByUrlWithHeader(String url, final Map<String, String> headers) {
//        ApacheHttpTransport transport = new ApacheHttpTransport();
//        HttpRequestFactory factory = transport.createRequestFactory(new HttpRequestInitializer() {
//            public void initialize(final HttpRequest request) throws IOException {
//                request.setConnectTimeout(0);
//                request.setReadTimeout(0);
//                request.setParser(new JacksonFactory().createJsonObjectParser());
//                final HttpHeaders httpHeaders = new HttpHeaders();
//                for (Map.Entry<String, String> e : headers.entrySet()) {
//                    httpHeaders.set(e.getKey(), e.getValue());
//                }
//                request.setHeaders(httpHeaders);
//            }
//        });
//        String jsonString;
//        try {
//            HttpRequest request = factory.buildGetRequest(new GenericUrl(url));
//            HttpResponse response = request.execute();
//            jsonString = response.parseAsString();
//        } catch (IOException e) {
//            e.printStackTrace();
//            jsonString = null;
//        }
//        return jsonString;
//    }
}