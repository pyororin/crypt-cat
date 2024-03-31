package pyororin.cryptcat.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pyororin.cryptcat.config.CoinCheckRequestConfig;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
class CoinCheckRepositoryTest {
    @Autowired
    CoinCheckRepository repository;

    @Autowired
    CoinCheckRequestConfig config;

    @Disabled
    @Test
    void retrieveTicker() {
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
        var repository = new CoinCheckRepository(restClient, config);
        var actual = repository.retrieveTicker(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build());
        mockServer.verify();
        assertEquals(actual.getLast(), 27390);
    }

    @Disabled
    @Test
    void getBalance() {
        System.out.println(repository.getBalance());
    }
}