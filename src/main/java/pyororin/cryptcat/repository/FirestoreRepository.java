package pyororin.cryptcat.repository;

import pyororin.cryptcat.repository.model.OrderTransaction;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface FirestoreRepository {
    void set(String group, OrderTransaction orderTransaction);

    void addSkipCount(String group) throws ExecutionException, InterruptedException;

    OrderTransaction getByGroup(String group) throws ExecutionException, InterruptedException;

    Map<String, OrderTransaction> getAll() throws ExecutionException, InterruptedException;

    void remove(String group);
}
