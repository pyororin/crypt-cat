package pyororin.cryptcat;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
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
            System.out.println("Secret value: " + secretValue);
            return secretValue;
        }
    }

    @RestController
    class HelloworldController {
        @GetMapping("/")
        String hello() {
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
