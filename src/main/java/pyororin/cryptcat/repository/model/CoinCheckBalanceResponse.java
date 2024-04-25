package pyororin.cryptcat.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@Builder
public class CoinCheckBalanceResponse {
    private boolean success;
    private BigDecimal jpy;
    private BigDecimal btc;

    @JsonProperty("jpyReserved")
    private BigDecimal jpy_reserved;

    @JsonProperty("btcReserved")
    private BigDecimal btc_reserved;

    @JsonProperty("jpyLendInUse")
    private BigDecimal jpy_lend_in_use;

    @JsonProperty("btcLendInUse")
    private BigDecimal btc_lend_in_use;

    @JsonProperty("jpyLent")
    private BigDecimal jpy_lent;

    @JsonProperty("btcLent")
    private BigDecimal btc_lent;

    @JsonProperty("jpyDebt")
    private BigDecimal jpy_debt;

    @JsonProperty("btcDebt")
    private BigDecimal btc_debt;

    @JsonProperty("jpyTsumitate")
    private BigDecimal jpy_tsumitate;

    @JsonProperty("btcTsumitate")
    private BigDecimal btc_tsumitate;
}