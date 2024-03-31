package pyororin.cryptcat.repository;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import pyororin.cryptcat.repository.model.Secret;

import java.io.IOException;

@RequiredArgsConstructor
@Repository
public class SecretManagerRepository {

    @Value("${project.id}")
    String projectId;

    public String retrieveSecret(Secret secret) throws IOException {
        try (var client = SecretManagerServiceClient.create()) {
            return client.accessSecretVersion(AccessSecretVersionRequest.newBuilder()
                    .setName(String.format("projects/%s/secrets/%s/versions/latest", this.projectId, secret.getValue()))
                    .build()).getPayload().getData().toStringUtf8();
        }
    }
}
