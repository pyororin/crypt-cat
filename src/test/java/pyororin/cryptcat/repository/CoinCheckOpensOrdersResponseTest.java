package pyororin.cryptcat.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pyororin.cryptcat.repository.model.CoinCheckOpensOrdersResponse;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CoinCheckOpensOrdersResponseTest {
    @MockBean
    Clock clock;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Instant.parse("2016-02-23T13:14:50.000Z"));
    }

    @Test
    void testFindOrdersOver24Hours() {
        // テストデータの作成
        var orders = List.of(
                CoinCheckOpensOrdersResponse.Order.builder()
                        .id(202835)
                        .orderType("buy")
                        .rate(BigDecimal.valueOf(26890))
                        .pair("btc_jpy")
                        .pendingAmount(BigDecimal.valueOf(0.5527))
                        .pendingMarketBuyAmount(null)
                        .stopLossRate(null)
                        .createdAt(Instant.parse("2015-01-10T05:55:38.000Z"))
                        .build(),
                CoinCheckOpensOrdersResponse.Order.builder()
                        .id(202836)
                        .orderType("sell")
                        .rate(BigDecimal.valueOf(26990))
                        .pair("btc_jpy")
                        .pendingAmount(BigDecimal.valueOf(0.77))
                        .pendingMarketBuyAmount(null)
                        .stopLossRate(null)
                        .createdAt(Instant.parse("2015-01-10T05:55:38.000Z"))
                        .build(),
                CoinCheckOpensOrdersResponse.Order.builder()
                        .id(38632107)
                        .orderType("buy")
                        .rate(null)
                        .pair("btc_jpy")
                        .pendingAmount(null)
                        .pendingMarketBuyAmount(BigDecimal.valueOf(10000.0))
                        .stopLossRate(BigDecimal.valueOf(50000.0))
                        .createdAt(Instant.parse("2016-02-23T12:14:50.000Z"))
                        .build()
        );

        var result = CoinCheckOpensOrdersResponse.builder()
                .success(true)
                .orders(orders)
                .build().findOrdersOver24Hours(clock);

        List<Long> actualOrderIds = result.stream()
                .map(CoinCheckOpensOrdersResponse.Order::getId)
                .collect(Collectors.toList());

        assertEquals(2, result.size());
        assertEquals(List.of(202835L, 202836L), actualOrderIds);
    }
}
