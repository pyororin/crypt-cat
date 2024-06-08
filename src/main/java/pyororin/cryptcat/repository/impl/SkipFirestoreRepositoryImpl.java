package pyororin.cryptcat.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import pyororin.cryptcat.repository.FirestoreRepository;
import pyororin.cryptcat.repository.model.OrderTransaction;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Repository
@Slf4j
@ConditionalOnProperty(name = "coincheck.actually", havingValue = "false", matchIfMissing = true)
public class SkipFirestoreRepositoryImpl implements FirestoreRepository {
    @Override
    public void set(String group, OrderTransaction orderTransaction) {
    }

    @Override
    public OrderTransaction getByGroup(String group) {
        return OrderTransaction.builder().build();
    }

    @Override
    public Map<String, OrderTransaction> getAll() {
        return new HashMap<>();
    }

    @Override
    public void remove(String group) {
    }
}
