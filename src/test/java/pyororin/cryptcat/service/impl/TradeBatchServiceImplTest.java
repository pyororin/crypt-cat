package pyororin.cryptcat.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import pyororin.cryptcat.batch.TradeBatchServiceImpl;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;
import pyororin.cryptcat.repository.model.CoinCheckTransactionsResponse;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
        tradeBatchService.transactions();
    }
}