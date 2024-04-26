package pyororin.cryptcat.repository.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import pyororin.cryptcat.config.OrderLogic;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
            return last.min(bid).add(last.subtract(bid).abs().multiply(BigDecimal.valueOf(0.05)));
        } else if (logic.equals(OrderLogic.MIDIUM)) {
            return last.min(ask);
        } else if (logic.equals(OrderLogic.LOW)) {
            return ask;
        } else if (logic.equals(OrderLogic.EVEN)) {
            return ask.add(bid).add(last).divide(BigDecimal.valueOf(3), 9, RoundingMode.HALF_EVEN);
        }
        return last;
    }

    public BigDecimal getFairSellPrice(OrderLogic logic) {
        if (logic.equals(OrderLogic.HIGH)) {
            return last.max(ask).subtract(last.subtract(ask).abs().multiply(BigDecimal.valueOf(0.05)));
        } else if (logic.equals(OrderLogic.MIDIUM)) {
            return last.max(bid);
        } else if (logic.equals(OrderLogic.LOW)) {
            return bid;
        } else if (logic.equals(OrderLogic.EVEN)) {
            return ask.add(bid).add(last).divide(BigDecimal.valueOf(3), 9, RoundingMode.HALF_EVEN);
        }
        return last;
    }
}
