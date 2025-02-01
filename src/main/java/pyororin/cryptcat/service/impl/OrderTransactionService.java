package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.repository.FirestoreRepository;
import pyororin.cryptcat.repository.model.OrderTransaction;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTransactionService {
    private final FirestoreRepository repository;

    public OrderTransaction get(String group) {
        try {
            return repository.getByGroup(group);
        } catch (ExecutionException | InterruptedException e) {
            return OrderTransaction.builder().build();
        }
    }

    public void set(String group, OrderTransaction orderTransaction) {
        repository.set(group, orderTransaction);
    }

    public void addSkipCount(String group) {
        try {
            repository.addSkipCount(group);
        } catch (ExecutionException | InterruptedException ignored) {}
    }

    public void remove(String group) {
        repository.remove(group);
    }


}
