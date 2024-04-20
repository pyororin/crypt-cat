package pyororin.cryptcat.repository;

import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;

public interface CoinCheckRepository {
    CoinCheckTickerResponse retrieveTicker(CoinCheckRequest request);

    void exchangeBuy(CoinCheckRequest request);

    void exchangeSell(CoinCheckRequest request);
}
