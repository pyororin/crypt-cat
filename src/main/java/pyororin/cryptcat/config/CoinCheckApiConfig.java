package pyororin.cryptcat.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ToString
@Component
@ConfigurationProperties(prefix = "coincheck")
public class CoinCheckApiConfig {
    private String host;
    private String key;
    private String secret;
}
