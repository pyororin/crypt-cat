package pyororin.cryptcat.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ToString
@Component
@ConfigurationProperties(prefix = "slack")
public class SlackConfig {
    private String token;
    private String channelId;
    private String mentionTarget;
}
