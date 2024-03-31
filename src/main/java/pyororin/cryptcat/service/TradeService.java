package pyororin.cryptcat.service;

import pyororin.cryptcat.repository.model.Pair;

import java.math.BigDecimal;

public interface TradeService {
    BigDecimal buy(Pair pair);

    BigDecimal sell(Pair pair);
}
