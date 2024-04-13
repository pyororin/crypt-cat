package pyororin.cryptcat.repository.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@Data
@ToString
public class CoinCheckTickerResponse {
    private int last;
    private int bid;
    private int ask;
    private int high;
    private int low;
    private String volume;
    private long timestamp;

    public BigDecimal getFairBuyPrice() {
        return BigDecimal.valueOf(Math.min(last, ask));
    }

    public BigDecimal getFairSellPrice() {
        return BigDecimal.valueOf(Math.max(last, bid));
    }
}
