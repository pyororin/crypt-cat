package pyororin.cryptcat.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.FirestoreRepository;
import pyororin.cryptcat.repository.model.CoinCheckResponse;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;
import pyororin.cryptcat.repository.model.OrderTransaction;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.TradeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class TradeJpyFixServiceV4ImplTest {
    @Autowired
    TradeService tradeJpyFixServiceV4Impl;

    @MockBean(name = "skipCoinCheckRepositoryImpl")
    CoinCheckRepository repository;

    @MockBean(name = "skipFirestoreRepositoryImpl")
    FirestoreRepository firestore;

    @Test
    void Buy() {
        var jpy = new BigDecimal("5502389");
        var ratio = new BigDecimal("9435234");
        var amount = jpy.divide(ratio, 9, RoundingMode.DOWN);
        System.out.println(amount);
        System.out.println(amount.multiply(ratio));
    }
    @Test
    void Sell() throws ExecutionException, InterruptedException {
        var request = OrderRequest.builder().build();
        request.setOrderType("sell");
        request.setRatio(BigDecimal.valueOf(2));
        when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(1000)).bid(new BigDecimal(999)).build());
        doReturn(CoinCheckResponse.builder().build()).when(repository).exchangeSellLimit(any());
        when(firestore.getByGroup(any()))
                .thenReturn(OrderTransaction.builder().build());
        doReturn(CoinCheckResponse.builder().build()).when(repository).exchangeSellLimit(any());

        tradeJpyFixServiceV4Impl.order(Pair.BTC_JPY, request);
        verify(repository, times(1)).exchangeSellLimit(any());
        verify(firestore, times(1)).getByGroup(any());
        verify(firestore, times(1)).set(any(), any());
    }
}