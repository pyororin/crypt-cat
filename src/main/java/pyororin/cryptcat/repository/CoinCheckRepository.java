package pyororin.cryptcat.repository;

import pyororin.cryptcat.repository.model.*;

public interface CoinCheckRepository {
    CoinCheckTickerResponse retrieveTicker(CoinCheckRequest request);

    CoinCheckBalanceResponse retrieveBalance();

    CoinCheckOpensOrdersResponse retrieveOpensOrders();

    CoinCheckTransactionsResponse retrieveOrdersTransactions();

    void exchangeBuy(CoinCheckRequest request);

    void exchangeSell(CoinCheckRequest request);

    void exchangeCancel(long id);
}
