package pyororin.cryptcat.repository.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@Data
@ToString
public class CoinCheckResponse {
    private final String success;
    private final int id;
    private final String rate;
    private final String amount;
    private final String orderType;
    private final String timeInForce;
    private final String stopLossRate;
    private final String pair;
    private final String createdAt;
}
