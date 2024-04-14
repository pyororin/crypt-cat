package pyororin.cryptcat.service;

import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.model.Pair;

import java.math.BigDecimal;

public interface TradeJpyFixService {
    BigDecimal order(Pair pair, OrderRequest orderRequest);

    void orderSplit(Pair pair, OrderRequest orderRequest);
}
