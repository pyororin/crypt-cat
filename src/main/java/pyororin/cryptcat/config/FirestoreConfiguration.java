package pyororin.cryptcat.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class FirestoreConfiguration {

    @Value("${project.id}")
    String projectId;

    @Bean
    Firestore database() throws IOException {
        return FirestoreOptions.getDefaultInstance().toBuilder()
                .setProjectId(projectId)
                .setDatabaseId("crypt-cat")
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build().getService();
    }
}
