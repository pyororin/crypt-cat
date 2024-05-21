package pyororin.cryptcat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderControlConfiguration {

    @Bean
    OrderTransactions orderTransactions() {
        return OrderTransactions.builder().build();
    }
}
