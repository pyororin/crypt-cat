package pyororin.cryptcat.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.CoinCheckResponse;

@RequiredArgsConstructor
@Repository
public class CoinCheckRepository {
    private final RestClient restClient;

    public CoinCheckResponse retrieveRate(CoinCheckRequest request) {
        return restClient.get()
                .uri("/api/rate/{rate}", request.getPair().getValue())
                .retrieve()
                .toEntity(CoinCheckResponse.class).getBody();
    }
}