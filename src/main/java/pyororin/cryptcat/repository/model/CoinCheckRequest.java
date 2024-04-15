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
    private final String amount;
    private final String market_buy_amount;
    private final BigDecimal price;
    private final BigDecimal rate;
    private final BigDecimal group;
}
