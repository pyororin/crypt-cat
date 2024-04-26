package pyororin.cryptcat.repository.model;

import org.junit.jupiter.api.Test;
import pyororin.cryptcat.config.OrderLogic;

import java.math.BigDecimal;

class CoinCheckTickerResponseTest {

    @Test
    void getFairBuyPrice() {
        var response = CoinCheckTickerResponse.builder()
                .ask(BigDecimal.valueOf(20000))
                .bid(BigDecimal.valueOf(10000))
                .last(BigDecimal.valueOf(13000)).build();
        System.out.println(response.getFairBuyPrice(OrderLogic.HIGH));
        System.out.println(response.getFairBuyPrice(OrderLogic.MIDIUM));
        System.out.println(response.getFairBuyPrice(OrderLogic.LOW));
        System.out.println(response.getFairBuyPrice(OrderLogic.EVEN));
    }

    @Test
    void getFairSellPrice() {
        var response = CoinCheckTickerResponse.builder()
                .ask(BigDecimal.valueOf(20000))
                .bid(BigDecimal.valueOf(10000))
                .last(BigDecimal.valueOf(17000)).build();
        System.out.println(response.getFairSellPrice(OrderLogic.HIGH));
        System.out.println(response.getFairSellPrice(OrderLogic.MIDIUM));
        System.out.println(response.getFairSellPrice(OrderLogic.LOW));
        System.out.println(response.getFairSellPrice(OrderLogic.EVEN));
    }
}