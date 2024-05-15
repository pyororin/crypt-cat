package pyororin.cryptcat.repository;

import pyororin.cryptcat.repository.model.*;

public interface CoinCheckRepository {
    CoinCheckTickerResponse retrieveTicker(CoinCheckRequest request);

    CoinCheckBalanceResponse retrieveBalance();

    CoinCheckOpensOrdersResponse retrieveOpensOrders();

    CoinCheckTransactionsResponse retrieveOrdersTransactions();

    CoinCheckResponse exchangeBuyLimit(CoinCheckRequest request);

    CoinCheckResponse exchangeSellLimit(CoinCheckRequest request);

    void exchangeBuyMarket(CoinCheckRequest request);

    void exchangeSellMarket(CoinCheckRequest request);

    boolean exchangeCancel(long id);
}
