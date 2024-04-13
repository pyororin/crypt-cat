package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.TradeAllService;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@ConditionalOnProperty(name = "coincheck.actually", havingValue = "true")
@RequiredArgsConstructor
public class TradeAllServiceImpl implements TradeAllService {
    private final CoinCheckRepository repository;

    private BigDecimal buy(Pair pair, OrderRequest orderRequest) {
        //"buy"
        //指値注文 現物取引 買い
        //
        //*rate注文のレート。（例）28000
        //*amount注文での量。（例）0.1

        /* 市場最終価格(ticker.last or ticker.ask) = rate */
        /* 所持金額 / 市場最終価格(ticker.last or ticker.ask) = amount */
        var balanceResponse = repository.getBalance();
        var tickerResponse = repository.retrieveTicker(CoinCheckRequest.builder().pair(pair).build());
        var amount = balanceResponse.getJpy().divide(tickerResponse.getFairBuyPrice(), 4, RoundingMode.HALF_DOWN);
        repository.exchangeBuy(Pair.BTC_JPY, tickerResponse.getFairBuyPrice(), amount);
        log.info("{} {} {} {} {} {} {}",
                value("kind", "exchange"),
                value("pair", "btc_jpy"),
                value("order_type", "market_buy"),
                value("market_buy_amount", amount),
                value("market_buy_price", balanceResponse.getJpy()),
                value("order_rate", tickerResponse.getFairBuyPrice()),
                value("group", orderRequest.getGroup()));
        return balanceResponse.getJpy();
    }

    private BigDecimal sell(Pair pair, OrderRequest orderRequest) {
        //"sell"
        //指値注文 現物取引 売り
        //
        //*rate注文のレート。（例）28000
        //*amount注文での量。（例）0.1

        /* 市場最終価格(ticker.last or ticker.ask) = rate */
        /* 所持量 = amount */
        var balanceResponse = repository.getBalance();
        var tickerResponse = repository.retrieveTicker(CoinCheckRequest.builder().pair(pair).build());
        var amount = balanceResponse.getBtc();
        repository.exchangeSell(Pair.BTC_JPY, tickerResponse.getFairBuyPrice(), amount);
        log.info("{} {} {} {} {} {} {}",
                value("kind", "exchange"),
                value("pair", "btc_jpy"),
                value("order_type", "market_sell"),
                value("market_sell_amount", amount),
                value("market_sell_price", balanceResponse.getJpy()),
                value("order_rate", tickerResponse.getFairSellPrice()),
                value("group", orderRequest.getGroup()));
        return tickerResponse.getFairBuyPrice().multiply(amount);
    }

    @Override
    public BigDecimal order(Pair pair, OrderRequest orderRequest) {
        if (orderRequest.isBuy()) {
            return this.buy(pair, orderRequest);
        }
        if (orderRequest.isSell()) {
            return this.sell(pair, orderRequest);
        }
        log.warn("BodyパラメータにorderTypeが無いか、buy|sell ではありません");
        return BigDecimal.valueOf(0);
    }
}
