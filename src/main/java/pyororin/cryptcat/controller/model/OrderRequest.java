package pyororin.cryptcat.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OrderRequest {
    @NotNull
    private String reason;
    @NotNull
    private String group;
    @NotNull
    private int range;

    @JsonProperty("order_type")
    private String orderType;

    public boolean isSell() {
        return "sell".equals(this.orderType);
    }

    public boolean isBuy() {
        return "buy".equals(this.orderType);
    }
}
