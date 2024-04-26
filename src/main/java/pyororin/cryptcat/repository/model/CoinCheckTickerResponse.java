package pyororin.cryptcat.repository.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import pyororin.cryptcat.config.OrderLogic;

import java.math.BigDecimal;

@Builder
@Data
@ToString
public class CoinCheckTickerResponse {
    private BigDecimal last;
    private BigDecimal bid;
    private BigDecimal ask;
    private BigDecimal high;
    private BigDecimal low;
    private String volume;
    private long timestamp;

    public BigDecimal getFairBuyPrice(OrderLogic logic) {
        if (logic.equals(OrderLogic.HIGH)) {
            return last.min(ask).add(last.subtract(ask).abs().multiply(BigDecimal.valueOf(0.05)));
        } else if (logic.equals(OrderLogic.MIDIUM)) {
            return last;
        } else if (logic.equals(OrderLogic.LOW)) {
            return ask;
        }
        return last;
    }

    public BigDecimal getFairSellPrice(OrderLogic logic) {
        if (logic.equals(OrderLogic.HIGH)) {
            return last.max(bid).subtract(last.subtract(bid).abs().multiply(BigDecimal.valueOf(0.05)));
        } else if (logic.equals(OrderLogic.MIDIUM)) {
            return last;
        } else if (logic.equals(OrderLogic.LOW)) {
            return bid;
        }
        return last;
    }
}
