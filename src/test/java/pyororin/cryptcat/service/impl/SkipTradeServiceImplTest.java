package pyororin.cryptcat.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;
import pyororin.cryptcat.repository.model.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
class SkipTradeServiceImplTest {
    @Autowired
    SkipTradeServiceImpl skipTradeService;

    @MockBean
    CoinCheckRepository repository;

    @Test
    void buy() {
        Mockito.when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(10665058).bid(10665059).ask(10665057).build());
        assertEquals(BigDecimal.valueOf(26662.6425).setScale(4, RoundingMode.HALF_EVEN), skipTradeService.buy(Pair.BTC_JPY, new OrderRequest()));
    }

    @Test
    void sell() {
        Mockito.when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(10665058).bid(10665059).ask(10665057).build());
        assertEquals(BigDecimal.valueOf(26662.6475).setScale(4, RoundingMode.HALF_EVEN), skipTradeService.sell(Pair.BTC_JPY, new OrderRequest()));
    }

    @Test
    void strategySell() {
        var request = new OrderRequest();
        request.setOrderType("sell");
        Mockito.when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(10665058).bid(10665059).ask(10665057).build());
        assertEquals(BigDecimal.valueOf(26662.6475).setScale(4, RoundingMode.HALF_EVEN), skipTradeService.order(Pair.BTC_JPY, request));
    }

    @Test
    void strategyBuy() {
        var request = new OrderRequest();
        request.setOrderType("buy");
        Mockito.when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(10665058).bid(10665059).ask(10665057).build());
        assertEquals(BigDecimal.valueOf(26662.6425).setScale(4, RoundingMode.HALF_EVEN), skipTradeService.order(Pair.BTC_JPY, request));
    }

    @Test
    void strategyOther() {
        var request = new OrderRequest();
        request.setOrderType("other");
        Mockito.when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(10665058).bid(10665059).ask(10665057).build());
        assertEquals(BigDecimal.valueOf(0), skipTradeService.order(Pair.BTC_JPY, request));
    }
}