package pyororin.cryptcat.repository.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

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
}
