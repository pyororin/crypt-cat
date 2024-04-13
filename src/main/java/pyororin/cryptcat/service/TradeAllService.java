package pyororin.cryptcat.service;

import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.model.Pair;

import java.math.BigDecimal;

public interface TradeAllService {
    BigDecimal order(Pair pair, OrderRequest orderRequest);
}
