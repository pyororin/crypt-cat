package pyororin.cryptcat.repository.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

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

    public BigDecimal getFairBuyPrice() {
//        return last;
        return last.min(ask);
    }

    public BigDecimal getFairSellPrice() {
//        return last;
        return last.max(bid);
    }
}
