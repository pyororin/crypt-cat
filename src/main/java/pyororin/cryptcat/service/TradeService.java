package pyororin.cryptcat.service;

import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.model.Pair;

import java.math.BigDecimal;

public interface TradeService {
    BigDecimal buy(Pair pair, OrderRequest orderRequest);

    BigDecimal sell(Pair pair, OrderRequest orderRequest);

    BigDecimal order(Pair pair, OrderRequest orderRequest);
}
