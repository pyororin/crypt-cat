package pyororin.cryptcat.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Data
@ToString
@Component
@ConfigurationProperties(prefix = "coincheck")
public class CoinCheckApiConfig {
    private String host;
    private BigDecimal amount;
    private String actually;
    private int interval;
}
