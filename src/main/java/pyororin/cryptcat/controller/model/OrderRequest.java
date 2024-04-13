package pyororin.cryptcat.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class OrderRequest {
    @NotNull
    private String reason;
    @NotNull
    private String group;
    private int range;
    @NotNull
    private BigDecimal ratio = BigDecimal.valueOf(1);

    @JsonProperty(value = "order-type", required = true)
    private String orderType;

    public boolean isSell() {
        return "sell".equals(this.orderType);
    }

    public boolean isBuy() {
        return "buy".equals(this.orderType);
    }
}
