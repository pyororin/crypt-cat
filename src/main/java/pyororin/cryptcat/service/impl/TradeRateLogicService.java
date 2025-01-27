package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.config.OrderLogic;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeRateLogicService {
    /*
    https://coincheck.com/ja/documents/exchange/api#ticker
    last 最後の取引の価格
    bid 現在の買い注文の最高価格
    ask 現在の売り注文の最安価格
     */
    private final CoinCheckRepository repository;
    private final CoinCheckApiConfig apiConfig;

    public BigDecimal getFairBuyRate(Pair pair) {
        return selectBuyRate(pair, apiConfig.getOrderLogic());
    }

    public BigDecimal getFairSellRate(Pair pair) {
        return selectSellRate(pair, apiConfig.getOrderLogic());
    }

    public BigDecimal selectBuyRate(Pair pair, OrderLogic orderLogic) {
        var tickerResponse = repository.retrieveTicker(CoinCheckRequest.builder().pair(pair).build());
        if (orderLogic.equals(OrderLogic.HIGH)) {
            return tickerResponse.getLast().min(tickerResponse.getBid())
                    .add(tickerResponse.getLast().subtract(tickerResponse.getBid()).abs().multiply(BigDecimal.valueOf(0.05)));
        } else if (orderLogic.equals(OrderLogic.MEDIUM)) {
            return tickerResponse.getBid().add(BigDecimal.valueOf(100));
        } else if (orderLogic.equals(OrderLogic.LOW)) {
            return tickerResponse.getAsk();
        } else if (orderLogic.equals(OrderLogic.EVEN)) {
            return tickerResponse.getBid().add(tickerResponse.getAsk()).add(tickerResponse.getLast())
                    .divide(BigDecimal.valueOf(3), 9, RoundingMode.HALF_EVEN);
        }
        return tickerResponse.getLast();
    }

    public BigDecimal selectSellRate(Pair pair, OrderLogic orderLogic) {
        var tickerResponse = repository.retrieveTicker(CoinCheckRequest.builder().pair(pair).build());
        if (orderLogic.equals(OrderLogic.HIGH)) {
            return tickerResponse.getLast().max(tickerResponse.getAsk())
                    .subtract(tickerResponse.getLast().subtract(tickerResponse.getAsk()).abs().multiply(BigDecimal.valueOf(0.05)));
        } else if (orderLogic.equals(OrderLogic.MEDIUM)) {
            return tickerResponse.getAsk().subtract(BigDecimal.valueOf(100));
        } else if (orderLogic.equals(OrderLogic.LOW)) {
            return tickerResponse.getBid();
        } else if (orderLogic.equals(OrderLogic.EVEN)) {
            return tickerResponse.getAsk().add(tickerResponse.getBid()).add(tickerResponse.getLast())
                    .divide(BigDecimal.valueOf(3), 9, RoundingMode.HALF_EVEN);
        }
        return tickerResponse.getLast();
    }
}
