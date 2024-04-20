package pyororin.cryptcat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;
import pyororin.cryptcat.repository.SecretManagerRepository;
import pyororin.cryptcat.repository.model.Secret;

import java.io.IOException;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
@RequiredArgsConstructor
@EnableRetry
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
                .accessKey("yYTQREbTx3uWJpoW")
                .secret("Ixy44AzSiXDi1vYQw6saA4ZCAt8Mfth6")
                .build();
    }

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Tokyo"));
    }
}
