package pyororin.cryptcat.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import pyororin.cryptcat.batch.TradeBatchServiceImpl;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckOpensOrdersResponse;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;
import pyororin.cryptcat.repository.model.CoinCheckTransactionsResponse;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = "coincheck.actually=true")
class TradeBatchServiceImplTest {
    @Autowired
    TradeBatchServiceImpl tradeBatchService;

    @MockBean
    CoinCheckRepository repository;

    @MockBean
    Clock clock;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Instant.parse("2016-02-23T13:04:50.000Z"));
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
        tradeBatchService.cancel();
    }

    @Test
    void orders() {
        when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder().last(new BigDecimal(1000)).build());
        when(repository.retrieveOrdersTransactions())
                .thenReturn(
                        CoinCheckTransactionsResponse.builder().data(List.of(
                                CoinCheckTransactionsResponse.Data.builder()
                                        .id(38L)
                                        .orderId(49)
                                        .createdAt(Instant.parse("2016-02-23T13:00:50.000Z"))
                                        .funds(CoinCheckTransactionsResponse.Funds.builder()
                                                .btc(BigDecimal.valueOf(0.1))
                                                .jpy(BigDecimal.valueOf(-4096.135)).build())
                                        .pair("btc_jpy")
                                        .rate(BigDecimal.valueOf(40900.0))
                                        .feeCurrency("btc_jpy")
                                        .fee(BigDecimal.valueOf(6.135))
                                        .liquidity("T")
                                        .side("buy")
                                        .build(),
                                CoinCheckTransactionsResponse.Data.builder()
                                        .id(37L)
                                        .orderId(48)
                                        .createdAt(Instant.parse("2016-02-23T12:00:50.000Z"))
                                        .funds(CoinCheckTransactionsResponse.Funds.builder()
                                                .btc(BigDecimal.valueOf(-0.1))
                                                .jpy(BigDecimal.valueOf(4064.09)).build())
                                        .pair("btc_jpy")
                                        .rate(BigDecimal.valueOf(40900.0))
                                        .feeCurrency("btc_jpy")
                                        .fee(BigDecimal.valueOf(-4.09))
                                        .liquidity("M")
                                        .side("sell")
                                        .build()
                        )).build());
        tradeBatchService.orders();
    }
}