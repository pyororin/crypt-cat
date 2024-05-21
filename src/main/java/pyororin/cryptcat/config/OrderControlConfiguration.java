package pyororin.cryptcat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class OrderControlConfiguration {

    @Bean
    OrderTransactions orderTransactions() {
        return OrderTransactions.builder().orderTransactions(new HashMap<>()).build();
    }
}
