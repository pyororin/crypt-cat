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
    void strategySell() {
        var request = new OrderRequest();
        request.setOrderType("sell");
        request.setRatio(BigDecimal.valueOf(2));
        Mockito.when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(10665058)).bid(new BigDecimal(10665059)).ask(new BigDecimal(10665057)).build());
        assertEquals(BigDecimal.valueOf(53325.2950).setScale(4, RoundingMode.HALF_EVEN), skipTradeService.order(Pair.BTC_JPY, request));
    }

    @Test
    void strategyBuy() {
        var request = new OrderRequest();
        request.setOrderType("buy");
        Mockito.when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(10665058)).bid(new BigDecimal(10665059)).ask(new BigDecimal(10665057)).build());
        assertEquals(BigDecimal.valueOf(26662.6425).setScale(4, RoundingMode.HALF_EVEN), skipTradeService.order(Pair.BTC_JPY, request));
    }

    @Test
    void strategyOther() {
        var request = new OrderRequest();
        request.setOrderType("other");
        Mockito.when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(10665058)).bid(new BigDecimal(10665059)).ask(new BigDecimal(10665057)).build());
        assertEquals(BigDecimal.valueOf(0), skipTradeService.order(Pair.BTC_JPY, request));
    }

    @Test
    void strategyOtherSplitBuy() {
        var request = new OrderRequest();
        request.setOrderType("buy");
        request.setRatio(BigDecimal.valueOf(3));
        Mockito.when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(10665058)).bid(new BigDecimal(10665059)).ask(new BigDecimal(10665057)).build());
        skipTradeService.orderSplit(Pair.BTC_JPY, request);
    }

    @Test
    void strategyOtherSplitSell() {
        var request = new OrderRequest();
        request.setOrderType("sell");
        request.setRatio(BigDecimal.valueOf(3));
        Mockito.when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(10665058)).bid(new BigDecimal(10665059)).ask(new BigDecimal(10665057)).build());
        skipTradeService.orderSplit(Pair.BTC_JPY, request);
    }
}