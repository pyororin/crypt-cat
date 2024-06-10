package pyororin.cryptcat.repository.impl;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import pyororin.cryptcat.config.OrderStatus;
import pyororin.cryptcat.repository.FirestoreRepository;
import pyororin.cryptcat.repository.model.OrderTransaction;
import pyororin.cryptcat.repository.model.OrderType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;

@RequiredArgsConstructor
@Repository
@Slf4j
@ConditionalOnProperty(name = "coincheck.actually", havingValue = "true")
public class FirestoreRepositoryImpl implements FirestoreRepository {
    private final Firestore database;

    public void set(String group, OrderTransaction orderTransaction) {
        Map<String, Object> record = new HashMap<>();
        record.put("orderId", orderTransaction.getOrderId());
        record.put("createdAt", orderTransaction.getCreatedAt());
        record.put("orderType", orderTransaction.getOrderType());
        record.put("orderStatus", orderTransaction.getOrderStatus());
        var result = database.collection("order-request").document(group).set(record);
        log.info("{} {}", value("kind", "firestore-update"), value("result", result.toString()));
    }

    public OrderTransaction getByGroup(String group) throws ExecutionException, InterruptedException {
        var docRef = database.collection("order-request").document(group);
        var snapshot = docRef.get().get();
        if (snapshot.exists()) {
            return OrderTransaction.builder()
                    .orderId(snapshot.getLong("orderId"))
                    .createdAt(snapshot.getString("createdAt"))
                    .orderType(OrderType.valueOf(snapshot.getString("orderType")))
                    .orderStatus(OrderStatus.valueOf(snapshot.getString("orderStatus"))).build();
        } else {
            return OrderTransaction.builder().build();
        }
    }

    public Map<String, OrderTransaction> getAll() throws ExecutionException, InterruptedException {
        var documents = database.collection("order-request").get().get().getDocuments();

        return documents.stream()
                .collect(Collectors.toMap(DocumentSnapshot::getId, document ->
                        OrderTransaction.builder().orderId(document.getLong("orderId"))
                                .createdAt(document.getString("createdAt"))
                                .orderType(OrderType.valueOf(document.getString("orderType")))
                                .orderStatus(OrderStatus.valueOf(document.getString("orderStatus")))
                                .build()));
    }

    public void remove(String group) {
        var docRef = database.collection("order-request").document(group);
        var result = docRef.delete();
        log.info("{} {}", value("kind", "firestore-update"), value("result", result.toString()));
    }
}
