package pyororin.cryptcat.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.CoinCheckResponse;
import pyororin.cryptcat.repository.model.Pair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
class CoinCheckRepositoryTest {
    @Autowired
    CoinCheckRepository repository;

    @Disabled
    @Test
    void retrieveRate() {
        var response = repository.retrieveRate(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build());
        System.out.println(response);
    }

    @Test
    void retrieveRateMock() {
        var restClientBuilder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer.expect(requestTo("/api/rate/btc_jpy"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"rate":"123456"}
                        """, MediaType.APPLICATION_JSON));

        var restClient = restClientBuilder.build();
        var repository = new CoinCheckRepository(restClient);

        CoinCheckResponse actual = repository.retrieveRate(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build());
        mockServer.verify();
        assertEquals(actual.getRate(), "123456");
    }
}