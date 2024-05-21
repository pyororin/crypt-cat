package pyororin.cryptcat.config;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Builder
@Data
@ToString
public class OrderTransactions {
    Map<String, OrderTransaction> orderTransactions;

    public OrderTransaction get(String group) {
        return orderTransactions.getOrDefault(group, OrderTransaction.builder().build());
    }

    public void remove(String group) {
        orderTransactions.remove(group);
    }

    public void put(String group, OrderTransaction orderTransaction) {
        orderTransactions.put(group, orderTransaction);
    }
}
