package pyororin.cryptcat.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.config.OrderLogic;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckTickerResponse;
import pyororin.cryptcat.repository.model.Pair;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class TradeRateLogicServiceTest {
    @Autowired
    TradeRateLogicService tradeRateLogicService;

    @MockBean(name = "skipCoinCheckRepositoryImpl")
    CoinCheckRepository repository;

    @BeforeEach
    void setUp() {
        when(repository.retrieveTicker(any()))
                .thenReturn(CoinCheckTickerResponse.builder()
                        .ask(BigDecimal.valueOf(20000))
                        .bid(BigDecimal.valueOf(10000))
                        .last(BigDecimal.valueOf(13000)).build());
    }

    @Nested
    class OrderLogicHigh {
        @Autowired
        CoinCheckApiConfig apiConfig;

        @BeforeEach
        void setUp() {
            apiConfig.setOrderLogic(OrderLogic.HIGH);
        }

        @Test
        void getFairBuyPrice() {
            System.out.println(tradeRateLogicService.getFairBuyPrice(Pair.BTC_JPY));
        }

        @Test
        void getFairSellPrice() {
            System.out.println(tradeRateLogicService.getFairSellPrice(Pair.BTC_JPY));
        }
    }

    @Nested
    class OrderLogicMedium {
        @Autowired
        CoinCheckApiConfig apiConfig;

        @BeforeEach
        void setUp() {
            apiConfig.setOrderLogic(OrderLogic.MEDIUM);
        }

        @Test
        void getFairBuyPrice() {
            System.out.println(tradeRateLogicService.getFairBuyPrice(Pair.BTC_JPY));
        }

        @Test
        void getFairSellPrice() {
            System.out.println(tradeRateLogicService.getFairSellPrice(Pair.BTC_JPY));
        }
    }

    @Nested
    class OrderLogicLow {
        @Autowired
        CoinCheckApiConfig apiConfig;

        @BeforeEach
        void setUp() {
            apiConfig.setOrderLogic(OrderLogic.LOW);
        }

        @Test
        void getFairBuyPrice() {
            System.out.println(tradeRateLogicService.getFairBuyPrice(Pair.BTC_JPY));
        }

        @Test
        void getFairSellPrice() {
            System.out.println(tradeRateLogicService.getFairSellPrice(Pair.BTC_JPY));
        }
    }

    @Nested
    class OrderLogicEven {
        @Autowired
        CoinCheckApiConfig apiConfig;

        @BeforeEach
        void setUp() {
            apiConfig.setOrderLogic(OrderLogic.EVEN);
        }

        @Test
        void getFairBuyPrice() {
            System.out.println(tradeRateLogicService.getFairBuyPrice(Pair.BTC_JPY));
        }

        @Test
        void getFairSellPrice() {
            System.out.println(tradeRateLogicService.getFairSellPrice(Pair.BTC_JPY));
        }
    }
}