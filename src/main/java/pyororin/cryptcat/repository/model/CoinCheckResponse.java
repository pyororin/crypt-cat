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
    private String success;
    private String error;
    private int id;
    private String rate;
    private String amount;
    @JsonProperty("order_type")
    private String orderType;
    @JsonProperty("time_in_force")
    private String timeInForce;
    @JsonProperty("stop_loss_rate")
    private String stopLossRate;
    private String pair;
    @JsonProperty("created_at")
    private String createdAt;
}
