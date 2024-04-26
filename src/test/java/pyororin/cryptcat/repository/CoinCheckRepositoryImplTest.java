package pyororin.cryptcat.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.config.CoinCheckRequestConfig;
import pyororin.cryptcat.repository.impl.CoinCheckRepositoryImpl;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@TestPropertySource(properties = "coincheck.actually=true")
class CoinCheckRepositoryImplTest {
    @Autowired
    Clock clock;

    @Autowired
    CoinCheckRepository repository;

    @Autowired
    CoinCheckRequestConfig config;

    @Autowired
    CoinCheckApiConfig apiConfig;

    @MockBean
    RestClient restClient;

    @Disabled
    @Test
    void retrieveTicker() {
        var restClient = RestClient.builder().baseUrl(apiConfig.getHost()).build();
        var repository = new CoinCheckRepositoryImpl(restClient, config, apiConfig);
        var response = repository.retrieveTicker(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build());
        System.out.println(response);
    }

    @Test
    void retrieveTickerMock() {
        var restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer.expect(requestTo("/api/ticker/?pair=btc_jpy"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "last": 27390,
                          "bid": 26900,
                          "ask": 27390,
                          "high": 27659,
                          "low": 26400,
                          "volume": "50.29627103",
                          "timestamp": 1423377841
                        }
                        """, MediaType.APPLICATION_JSON));

        var restClient = restClientBuilder.build();
        var repository = new CoinCheckRepositoryImpl(restClient, config, apiConfig);
        var actual = repository.retrieveTicker(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build());
        mockServer.verify();
        assertEquals(actual.getLast(), new BigDecimal(27390));
    }

    @Test
    void exchangeMock_ok() {
        var restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer.expect(requestTo("/api/exchange/orders"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "success": true,
                          "id": 12345,
                          "rate": "30010.0",
                          "amount": "1.3",
                          "order_type": "sell",
                          "time_in_force": "good_til_cancelled",
                          "stop_loss_rate": null,
                          "pair": "btc_jpy",
                          "created_at": "2015-01-10T05:55:38.000Z"
                        }
                        """, MediaType.APPLICATION_JSON));

        var restClient = restClientBuilder.build();
        var repository = new CoinCheckRepositoryImpl(restClient, config, apiConfig);
        repository.exchangeBuy(CoinCheckRequest.builder()
                .pair(Pair.BTC_JPY)
                .price(BigDecimal.valueOf(30010.0))
                .amount(BigDecimal.valueOf(1.3))
                .rate(BigDecimal.valueOf(39013)).build());
        mockServer.verify();
    }

    @Test
    void exchangeMock_ng() {
        var restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer.expect(requestTo("/api/exchange/orders"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "success": false,
                          "id": 12345,
                          "created_at": "2015-01-10T05:55:38.000Z"
                        }
                        """, MediaType.APPLICATION_JSON));

        var restClient = restClientBuilder.build();
        var repository = new CoinCheckRepositoryImpl(restClient, config, apiConfig);
        repository.exchangeBuy(CoinCheckRequest.builder()
                .pair(Pair.BTC_JPY)
                .price(BigDecimal.valueOf(30010.0))
                .amount(BigDecimal.valueOf(1.3))
                .rate(BigDecimal.valueOf(39013)).build());
        mockServer.verify();
    }

    @Disabled
    @Test
    void retrieveBalance() {
        var repository = new CoinCheckRepositoryImpl(
                RestClient.builder().baseUrl(apiConfig.getHost()).build(), config, apiConfig);
        System.out.println(repository.retrieveBalance());
    }

    @Disabled
    @Test
    void retrieveOpensOrders() {
        var repository = new CoinCheckRepositoryImpl(
                RestClient.builder().baseUrl(apiConfig.getHost()).build(), config, apiConfig);
        System.out.println(repository.retrieveOpensOrders());
    }

    @Disabled
    @Test
    void exchangeBuy() {
        var repository = new CoinCheckRepositoryImpl(
                RestClient.builder().baseUrl(apiConfig.getHost()).build(), config, apiConfig);
        var response = repository.retrieveTicker(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build());
        var rate = response.getFairBuyPrice();
        var amount = BigDecimal.valueOf(0.0055);
        repository.exchangeBuy(CoinCheckRequest.builder().pair(Pair.BTC_JPY).price(rate).amount(amount).build());
    }

    @Disabled
    @Test
    void exchangeSell() {
        var response = repository.retrieveTicker(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build());
        var rate = response.getFairSellPrice();
        var amount = BigDecimal.valueOf(0.0055);
        repository.exchangeSell(CoinCheckRequest.builder().pair(Pair.BTC_JPY).price(rate).amount(amount).build());
    }

    @Test
    void retryTicker() {
        doThrow(new RuntimeException("Test")).when(restClient).get();
        assertThrows(RestClientException.class, () -> repository.retrieveTicker(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build()));
        verify(restClient, times(5)).get();
    }

    @Test
    void retryExchange() {
        var restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer.expect(requestTo("/api/jpyfix/btc_jpy"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "success": true,
                          "id": 12345,
                          "rate": "30010.0",
                          "amount": "1.3",
                          "order_type": "sell",
                          "time_in_force": "good_til_cancelled",
                          "stop_loss_rate": null,
                          "pair": "btc_jpy",
                          "created_at": "2015-01-10T05:55:38.000Z"
                        }
                        """, MediaType.APPLICATION_JSON));

        doThrow(new RuntimeException("Test")).when(restClient).post();
        assertThrows(RestClientException.class, () -> {
            repository.exchangeBuy(CoinCheckRequest.builder()
                    .pair(Pair.BTC_JPY)
                    .price(BigDecimal.valueOf(30010.0))
                    .amount(BigDecimal.valueOf(1.3))
                    .rate(BigDecimal.valueOf(39013)).build());
            mockServer.verify();
        });
        verify(restClient, times(5)).post();
    }

    @Disabled
    @Test
    void orders() {
        var repository = new CoinCheckRepositoryImpl(
                RestClient.builder().baseUrl(apiConfig.getHost()).build(), config, apiConfig);
        var response = repository.retrieveOrdersTransactions();
        System.out.println(response);
        System.out.println(response.sumFunds(Clock.system(ZoneId.of("Asia/Tokyo"))));
        var ticker = repository.retrieveTicker(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build());
        System.out.println(ticker);
        var jpyToBtc = response.sumFunds(clock).getJpy().divide(ticker.getLast(), 9, RoundingMode.HALF_EVEN);
        System.out.println(jpyToBtc);
        System.out.println(response.sumFunds(clock).getBtc().add(jpyToBtc));
    }
}