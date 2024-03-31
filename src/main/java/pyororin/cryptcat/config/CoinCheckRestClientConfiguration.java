package pyororin.cryptcat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class CoinCheckRestClientConfiguration {
    private final CoinCheckApiConfig config;

    @Bean
    RestClient restClient() {
        return RestClient.builder().baseUrl(config.getHost()).build();
    }
}