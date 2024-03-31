package pyororin.cryptcat.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;
import pyororin.cryptcat.repository.model.Pair;

import java.math.BigDecimal;

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
                .thenReturn(CoinCheckTickerResponse.builder().last(123456).build());
        assertEquals(skipTradeService.buy(Pair.BTC_JPY), BigDecimal.valueOf(0.123456));
    }

    @Test
    void sell() {
        assertEquals(skipTradeService.sell(Pair.BTC_JPY), BigDecimal.valueOf(0.000001));
    }
}