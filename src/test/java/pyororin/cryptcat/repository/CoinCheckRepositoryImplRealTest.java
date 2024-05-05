package pyororin.cryptcat.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Clock;
import java.util.stream.LongStream;

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