package pyororin.cryptcat.repository.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@Data
@ToString
public class CoinCheckBalanceResponse {
    private boolean success;
    private BigDecimal jpy;
    private BigDecimal btc;
    private BigDecimal eth;
    private BigDecimal etc;
    private BigDecimal lsk;
    private BigDecimal xrp;
    private BigDecimal xem;
    private BigDecimal ltc;
    private BigDecimal bch;
    private BigDecimal mona;
    private BigDecimal xlm;
    private BigDecimal qtum;
    private BigDecimal bat;
    private BigDecimal iost;
    private BigDecimal enj;
    private BigDecimal plt;
    private BigDecimal sand;
    private BigDecimal xym;
    private BigDecimal dot;
    private BigDecimal flr;
    private BigDecimal fnct;
    private BigDecimal chz;
    private BigDecimal link;
    private BigDecimal dai;
    private BigDecimal mkr;
    private BigDecimal matic;
    private BigDecimal ape;
    private BigDecimal axs;
    private BigDecimal imx;
    private BigDecimal wbtc;
    private BigDecimal shib;
    private BigDecimal avax;
}