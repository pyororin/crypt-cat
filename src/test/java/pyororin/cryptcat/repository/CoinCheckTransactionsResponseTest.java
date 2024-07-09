package pyororin.cryptcat.repository;

import org.junit.jupiter.api.Test;
import pyororin.cryptcat.repository.model.CoinCheckTransactionsResponse;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class CoinCheckTransactionsResponseTest {

    @Test
    public void testAggregateByRate() {
        // Arrange
        CoinCheckTransactionsResponse.Data data1 = CoinCheckTransactionsResponse.Data.builder()
                .id(1L)
                .orderId(1001L)
                .createdAt(Instant.now())
                .funds(CoinCheckTransactionsResponse.Funds.builder()
                        .btc(new BigDecimal("0.5"))
                        .jpy(new BigDecimal("1000"))
                        .build())
                .pair("BTC_JPY")
                .rate(new BigDecimal("5000000"))
                .feeCurrency("JPY")
                .fee(new BigDecimal("10"))
                .liquidity("T")
                .side("buy")
                .build();

        CoinCheckTransactionsResponse.Data data2 = CoinCheckTransactionsResponse.Data.builder()
                .id(2L)
                .orderId(1002L)
                .createdAt(Instant.now())
                .funds(CoinCheckTransactionsResponse.Funds.builder()
                        .btc(new BigDecimal("0.3"))
                        .jpy(new BigDecimal("600"))
                        .build())
                .pair("BTC_JPY")
                .rate(new BigDecimal("5000005")) // Slightly different rate within ±10
                .feeCurrency("JPY")
                .fee(new BigDecimal("5"))
                .liquidity("T")
                .side("buy")
                .build();

        CoinCheckTransactionsResponse.Data data3 = CoinCheckTransactionsResponse.Data.builder()
                .id(3L)
                .orderId(1003L)
                .createdAt(Instant.now())
                .funds(CoinCheckTransactionsResponse.Funds.builder()
                        .btc(new BigDecimal("0.2"))
                        .jpy(new BigDecimal("400"))
                        .build())
                .pair("BTC_JPY")
                .rate(new BigDecimal("4999999")) // Slightly different rate within ±10
                .feeCurrency("JPY")
                .fee(new BigDecimal("2"))
                .liquidity("T")
                .side("buy")
                .build();


        // Act
        List<CoinCheckTransactionsResponse.Data> result =
                CoinCheckTransactionsResponse.builder().data(Arrays.asList(data1, data2, data3)).build().aggregateByRate();

        // Assert
        assertEquals(1, result.size());
        CoinCheckTransactionsResponse.Data aggregatedData = result.get(0);
        assertEquals(new BigDecimal("1.0"), aggregatedData.getFunds().getBtc());
        assertEquals(new BigDecimal("2000"), aggregatedData.getFunds().getJpy());
        assertEquals(new BigDecimal("17"), aggregatedData.getFee());
    }
}
