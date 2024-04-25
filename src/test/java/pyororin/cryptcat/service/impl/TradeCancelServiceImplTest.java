package pyororin.cryptcat.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckOpensOrdersResponse;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = "coincheck.actually=true")
class TradeCancelServiceImplTest {
    @Autowired
    TradeCancelServiceImpl tradeCancelServiceImpl;

    @MockBean
    CoinCheckRepository repository;

    @MockBean
    Clock clock;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Instant.parse("2016-02-23T13:14:50.000Z"));
    }

    @Test
    void cancel() {
        when(repository.retrieveOpensOrders())
                .thenReturn(
                        CoinCheckOpensOrdersResponse.builder().orders(List.of(
                                CoinCheckOpensOrdersResponse.Order.builder()
                                        .id(202835L)
                                        .orderType("buy")
                                        .rate(BigDecimal.valueOf(26890))
                                        .pair("btc_jpy")
                                        .pendingAmount(BigDecimal.valueOf(0.5527))
                                        .pendingMarketBuyAmount(null)
                                        .stopLossRate(null)
                                        .createdAt(Instant.parse("2016-02-24T12:14:50.000Z"))
                                        .build(),
                                CoinCheckOpensOrdersResponse.Order.builder()
                                        .id(202836L)
                                        .orderType("sell")
                                        .rate(BigDecimal.valueOf(26990))
                                        .pair("btc_jpy")
                                        .pendingAmount(BigDecimal.valueOf(0.77))
                                        .pendingMarketBuyAmount(null)
                                        .stopLossRate(null)
                                        .createdAt(Instant.parse("2016-02-22T12:14:50.000Z"))
                                        .build(),
                                CoinCheckOpensOrdersResponse.Order.builder()
                                        .id(38632107L)
                                        .orderType("buy")
                                        .rate(null)
                                        .pair("btc_jpy")
                                        .pendingAmount(null)
                                        .pendingMarketBuyAmount(BigDecimal.valueOf(10000.0))
                                        .stopLossRate(BigDecimal.valueOf(50000.0))
                                        .createdAt(Instant.parse("2016-02-23T12:14:50.000Z"))
                                        .build()
                        )).build());
        doNothing().when(repository).exchangeCancel(202836L);
        tradeCancelServiceImpl.cancel();
    }
}