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
            log.info("hello {} {}", value("params1", "value1"), value("params2", "value2"));
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
