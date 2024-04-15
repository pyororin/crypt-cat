package pyororin.cryptcat.repository;

import org.json.JSONObject;
import pyororin.cryptcat.repository.model.CoinCheckBalanceResponse;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;
import pyororin.cryptcat.repository.model.Pair;

import java.math.BigDecimal;

public interface CoinCheckRepository {
    CoinCheckTickerResponse retrieveTicker(CoinCheckRequest request);

    void exchangeBuy(CoinCheckRequest request);

    void exchangeSell(CoinCheckRequest request);
}
