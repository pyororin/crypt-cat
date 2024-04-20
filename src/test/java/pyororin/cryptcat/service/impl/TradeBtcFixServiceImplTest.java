package pyororin.cryptcat.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.impl.CoinCheckRepositoryImpl;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.TradeService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class TradeBtcFixServiceImplTest {
    @Autowired
    TradeService tradeBtcFixServiceImpl;

    @MockBean(name = "skipCoinCheckRepositoryImpl")
    CoinCheckRepositoryImpl repository;

    @Test
    void Sell() {
        var request = new OrderRequest();
        request.setOrderType("sell");
        request.setRatio(BigDecimal.valueOf(2));
        when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(1000)).bid(new BigDecimal(999)).build());
        doNothing().when(repository).exchangeSell(any());
        tradeBtcFixServiceImpl.order(Pair.BTC_JPY, request);
        verify(repository, times(2)).exchangeSell(any());
        verify(repository, times(0)).exchangeBuy(any());
    }

    @Test
    void buy() {
        var request = new OrderRequest();
        request.setOrderType("buy");
        request.setRatio(BigDecimal.valueOf(2));
        when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(1000)).ask(new BigDecimal(1001)).build());
        doNothing().when(repository).exchangeBuy(any());
        tradeBtcFixServiceImpl.order(Pair.BTC_JPY, request);
        verify(repository, times(2)).exchangeBuy(any());
        verify(repository, times(0)).exchangeSell(any());
    }

    @Test
    void other() {
        var request = new OrderRequest();
        request.setOrderType("other");
        request.setRatio(BigDecimal.valueOf(2));
        when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(1000)).bid(new BigDecimal(999)).ask(new BigDecimal(1001)).build());
        doNothing().when(repository).exchangeBuy(any());
        tradeBtcFixServiceImpl.order(Pair.BTC_JPY, request);
        verify(repository, times(0)).exchangeSell(any());
        verify(repository, times(0)).exchangeBuy(any());
    }
}