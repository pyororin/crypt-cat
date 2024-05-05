package pyororin.cryptcat.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.config.CoinCheckRequestConfig;
import pyororin.cryptcat.repository.impl.CoinCheckRepositoryImpl;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.impl.TradeRateLogicService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@TestPropertySource(properties = "coincheck.actually=true")
class CoinCheckRepositoryImplRealTest {
    @Autowired
    Clock clock;

    @Autowired
    CoinCheckRepository repository;

    @Disabled
    @Test
    void tryNonseValue() {
        LongStream.range(0, 10).forEach(i -> {
            try {
                System.out.println(i + ":" + repository.retrieveOpensOrders());
            } catch (Exception re) {
                System.out.println(i + re.getMessage());
            }
//            try {
//                TimeUnit.MILLISECONDS.sleep(10);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
        });
    }
}