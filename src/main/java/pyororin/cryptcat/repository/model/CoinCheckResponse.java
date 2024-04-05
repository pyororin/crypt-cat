package pyororin.cryptcat.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@Data
@ToString
public class CoinCheckResponse {
    /* {"datetime":"2024-03-31T18:52:50.271","version":"1","message":"{\"success\":true,\"id\":6306219886,\"amount\":null,\"rate\":null,\"order_type\":\"market_buy\",\"pair\":\"btc_jpy\",\"created_at\":\"2024-03-31T09:52:50.000Z\",\"market_buy_amount\":\"532805.3\",\"time_in_force\":\"good_til_cancelled\",\"stop_loss_rate\":null}" */
    private final String success;
    private final int id;
    private final String rate;
    private final String amount;
    @JsonProperty("order_type")
    private final String orderType;
    @JsonProperty("time_in_force")
    private final String timeInForce;
    @JsonProperty("stop_loss_rate")
    private final String stopLossRate;
    private final String pair;
    @JsonProperty("created_at")
    private final String createdAt;
}
