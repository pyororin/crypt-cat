package pyororin.cryptcat.service.impl;

import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
class TradeJpyFixServiceImplTest {
    @Autowired
    TradeJpyFixServiceImpl tradeJpyFixService;

    @MockBean
    CoinCheckRepository repository;

    @Test
    void strategySell() {
        var request = new OrderRequest();
        request.setOrderType("sell");
        request.setRatio(BigDecimal.valueOf(1));
        when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(1000)).bid(new BigDecimal(999)).build());
        doNothing().when(repository).exchangeSell(any(), any(), any());
        assertEquals(BigDecimal.valueOf(25.000000).setScale(6, RoundingMode.HALF_EVEN), tradeJpyFixService.order(Pair.BTC_JPY, request));
    }

    @Test
    void strategyBuy() {
        var request = new OrderRequest();
        request.setOrderType("buy");
        request.setRatio(BigDecimal.valueOf(1));
        when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(1000)).ask(new BigDecimal(1001)).build());
        doNothing().when(repository).exchangeBuy(any(), any(), any());
        assertEquals(BigDecimal.valueOf(25.000000).setScale(6, RoundingMode.HALF_EVEN), tradeJpyFixService.order(Pair.BTC_JPY, request));
    }

    @Test
    void strategyOther() {
        var request = new OrderRequest();
        request.setOrderType("other");
        request.setRatio(BigDecimal.valueOf(1));
        when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(1000)).bid(new BigDecimal(999)).ask(new BigDecimal(1001)).build());
        doNothing().when(repository).exchangeBuy(any(), any(), any());
        assertEquals(BigDecimal.valueOf(0), tradeJpyFixService.order(Pair.BTC_JPY, request));
    }

    @Test
    void strategyOtherSplitBuy() {
        var request = new OrderRequest();
        request.setOrderType("buy");
        request.setRatio(BigDecimal.valueOf(2));
        when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(1000)).bid(new BigDecimal(999)).ask(new BigDecimal(1001)).build());
        doNothing().when(repository).exchangeBuy(any(), any(), any());
        tradeJpyFixService.orderSplit(Pair.BTC_JPY, request);
    }

    @Test
    void strategyOtherSplitSell() {
        var request = new OrderRequest();
        request.setOrderType("sell");
        request.setRatio(BigDecimal.valueOf(2));
        when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(1000)).bid(new BigDecimal(1001)).ask(new BigDecimal(999)).build());
        doNothing().when(repository).exchangeSell(any(), any(), any());
        tradeJpyFixService.orderSplit(Pair.BTC_JPY, request);
    }
}