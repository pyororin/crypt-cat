package pyororin.cryptcat.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pyororin.cryptcat.config.CoinCheckApiConfig;
import pyororin.cryptcat.config.CoinCheckRequestConfig;
import pyororin.cryptcat.repository.impl.SkipCoinCheckRepositoryImpl;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
class SkipCoinCheckRepositoryImplTest {
    @Autowired
    CoinCheckRepository repository;

    @Autowired
    CoinCheckRequestConfig config;

    @Autowired
    CoinCheckApiConfig apiConfig;

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
        var repository = new SkipCoinCheckRepositoryImpl(restClient);
        var actual = repository.retrieveTicker(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build());
        mockServer.verify();
        assertEquals(actual.getLast(), new BigDecimal(27390));
    }

    @Test
    void exchangeMock_ok() {
        var restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer.expect(ExpectedCount.never(), requestTo("/api/exchange/orders"))
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
        var repository = new SkipCoinCheckRepositoryImpl(restClient);
        repository.exchangeBuy(CoinCheckRequest.builder().pair(Pair.BTC_JPY).price(BigDecimal.valueOf(30010.0)).amount(BigDecimal.valueOf(1.3)).build());
        mockServer.verify();
    }

    @Test
    void exchangeMock_ng() {
        var restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer.expect(ExpectedCount.never(), requestTo("/api/exchange/orders"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "success": false,
                          "id": 12345,
                          "created_at": "2015-01-10T05:55:38.000Z"
                        }
                        """, MediaType.APPLICATION_JSON));

        var restClient = restClientBuilder.build();
        var repository = new SkipCoinCheckRepositoryImpl(restClient);
        repository.exchangeBuy(CoinCheckRequest.builder().pair(Pair.BTC_JPY).price(BigDecimal.valueOf(30010.0)).amount(BigDecimal.valueOf(1.3)).build());
        mockServer.verify();
    }

//    @Disabled
//    @Test
//    void getBalance() {
//        System.out.println(repository.getBalance());
//    }
}