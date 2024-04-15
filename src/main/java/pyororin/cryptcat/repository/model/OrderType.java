package pyororin.cryptcat.repository.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum OrderType {
    BUY("buy"),
    SELL("sell"),
    MARKET_BUY("market_buy"),
    MARKET_SELL("market_sell");

    private final String code;
}