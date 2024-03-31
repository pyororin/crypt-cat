package pyororin.cryptcat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;
import pyororin.cryptcat.repository.SecretManagerRepository;
import pyororin.cryptcat.repository.model.Secret;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class CoinCheckRestClientConfiguration {
    private final CoinCheckApiConfig config;
    private final SecretManagerRepository secretManagerRepository;

    @Bean
    RestClient restClient() {
        return RestClient.builder().baseUrl(config.getHost()).build();
    }

    @Bean
    @Primary
    @Profile("production")
    CoinCheckRequestConfig coinCheckRequestConfig() throws IOException {
        return CoinCheckRequestConfig.builder()
                .accessKey(secretManagerRepository.retrieveSecret(Secret.COINCHECK_API_ACCESS_KEY))
                .secret(secretManagerRepository.retrieveSecret(Secret.COINCHECK_API_SECRET))
                .build();
    }

    @Bean
    @Profile("test")
    CoinCheckRequestConfig skipCoinCheckRequestConfig() {
        return CoinCheckRequestConfig.builder()
                .build();
    }
}