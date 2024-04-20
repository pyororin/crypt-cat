package pyororin.cryptcat.repository;

import pyororin.cryptcat.repository.model.CoinCheckBalanceResponse;
import pyororin.cryptcat.repository.model.CoinCheckOpensOrdersResponse;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;

public interface CoinCheckRepository {
    CoinCheckTickerResponse retrieveTicker(CoinCheckRequest request);

    CoinCheckBalanceResponse retrieveBalance();

    CoinCheckOpensOrdersResponse retrieveOpensOrders();

    void exchangeBuy(CoinCheckRequest request);

    void exchangeSell(CoinCheckRequest request);

    void exchangeCancel(long id);
}
