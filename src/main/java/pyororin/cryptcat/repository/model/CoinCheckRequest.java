package pyororin.cryptcat.repository.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@Data
@ToString
public class CoinCheckRequest {
    private final String orderType;
    private final Pair pair;
    private final String amount;
    private final String market_buy_amount;
}
