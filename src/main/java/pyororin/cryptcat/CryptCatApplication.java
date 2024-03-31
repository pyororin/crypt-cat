package pyororin.cryptcat;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.logstash.logback.argument.StructuredArguments.value;

@SpringBootApplication
@Slf4j
public class CryptCatApplication {

    @Value("${NAME:World}")
    String name;

    public static void main(String[] args) {
        SpringApplication.run(CryptCatApplication.class, args);
    }

    public String quickstart(String projectId, String secretId) throws Exception {
        // Secret Managerのクライアントを作成します
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            // シークレットのバージョンを指定してアクセスリクエストを作成します
            String secretName = String.format("projects/%s/secrets/%s/versions/latest", projectId, secretId);
            AccessSecretVersionRequest request = AccessSecretVersionRequest.newBuilder()
                    .setName(secretName)
                    .build();

            // シークレットの値を取得します
            SecretPayload payload = client.accessSecretVersion(request).getPayload();
            String secretValue = payload.getData().toStringUtf8();

            // シークレットの値を表示します
            log.debug("Secret value: " + secretValue);
            return secretValue;
        }
    }

    @RestController
    class HelloWorldController {
        @GetMapping("/")
        String hello() {
            log.info("{} {}",
                    value("kind", "alert"),
                    value("alert_type", "buy"));
            log.info("{} {}",
                    value("kind", "alert"),
                    value("alert_type", "sell"));
            log.info("{} {} {} {} {}",
                    value("kind", "rate"),
                    value("pair", "btc_jpy"),
                    value("order_type", "buy"),
                    value("amount", "0.003"),
                    value("price", "3000000"));
            log.info("{} {} {} {} {}",
                    value("kind", "rate"),
                    value("pair", "btc_jpy"),
                    value("order_type", "sell"),
                    value("amount", "0.004"),
                    value("price", "4000000"));
            log.info("{} {} {} {}",
                    value("kind", "exchange"),
                    value("pair", "btc_jpy"),
                    value("order_type", "market_buy"),
                    value("market_buy_amount", "10000"));
            log.info("{} {} {} {}",
                    value("kind", "exchange"),
                    value("pair", "btc_jpy"),
                    value("order_type", "market_sell"),
                    value("amount", "0.002"));
            log.info("{} {} {} {}",
                    value("kind", "exchange-skip"),
                    value("pair", "btc_jpy"),
                    value("order_type", "market_buy"),
                    value("market_buy_amount", "10000"));
            log.info("{} {} {} {}",
                    value("kind", "exchange-skip"),
                    value("pair", "btc_jpy"),
                    value("order_type", "market_sell"),
                    value("amount", "0.002"));
            return "Hello " + name + "!";
        }

        @GetMapping("/secret")
        String secret() throws Exception {
            String projectId = "cryptocurrency-auto-trade";
            String secretId = "token-test";
            return quickstart(projectId, secretId);
        }
    }
}
