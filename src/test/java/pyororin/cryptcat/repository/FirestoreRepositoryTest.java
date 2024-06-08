package pyororin.cryptcat.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pyororin.cryptcat.config.OrderStatus;
import pyororin.cryptcat.repository.model.OrderTransaction;
import pyororin.cryptcat.repository.model.OrderType;

import java.util.concurrent.ExecutionException;

@SpringBootTest
class FirestoreRepositoryTest {

    @Autowired
    FirestoreRepository repository;

    @Test
    @Disabled
    void put() {
        var records = OrderTransaction.builder()
                .orderId(100L)
                .createdAt("test-createdAt")
                .orderType(OrderType.SELL)
                .orderStatus(OrderStatus.ORDERED)
                .build();
        System.out.println(records);
        repository.put("test-group", records);
    }

    @Test
    @Disabled
    void remove() {
        repository.remove("test-group");
    }

    @Test
    @Disabled
    void getByGroup() throws ExecutionException, InterruptedException {
        System.out.println(repository.getByGroup("test-group"));
    }

    @Test
    @Disabled
    void getByGroupNoRecord() throws ExecutionException, InterruptedException {
        System.out.println(repository.getByGroup("non-exists-record"));
    }
}