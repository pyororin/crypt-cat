package pyororin.cryptcat.config;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CoinCheckRequestConfig {
    private String accessKey;
    private String secret;
}
