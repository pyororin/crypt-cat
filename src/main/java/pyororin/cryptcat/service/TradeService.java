package pyororin.cryptcat.service;

import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.model.Pair;

public interface TradeService {
    void order(Pair pair, OrderRequest orderRequest);
}
