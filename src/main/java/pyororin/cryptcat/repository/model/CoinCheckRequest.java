package pyororin.cryptcat.repository.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@Data
@ToString
public class CoinCheckRequest {
    private final OrderType orderType;
    private final Pair pair;
    private final BigDecimal amount;
    private final BigDecimal market_buy_amount;
    private final BigDecimal price;
    private final BigDecimal rate;
    private final String group;
}
