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
    private BigDecimal price;
    private String actually;
    private long interval;
    private long cancelDelayMinutes;
    private OrderLogic orderLogic;

    @Data
    @ToString
    @Component
    @ConfigurationProperties(prefix = "coincheck.retry")
    public static class Retry {
        private long delaySec;
        private int limitCount;
    }
}
