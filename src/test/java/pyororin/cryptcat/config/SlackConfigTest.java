package pyororin.cryptcat.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SlackConfigTest {
    @Autowired
    SlackConfig slackConfig;

    @Test
    void AutoConfigurationTest() {
        assertEquals("test01", slackConfig.getToken());
        assertEquals("test02", slackConfig.getChannelId());
        assertEquals("test03", slackConfig.getMentionTarget());
    }
}