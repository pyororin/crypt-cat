package pyororin.cryptcat.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pyororin.cryptcat.repository.model.Secret;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
class SecretManagerRepositoryTest {

    @MockBean
    SecretManagerRepository repository;

    @Disabled
    @Test
    void retrieveSecret() throws IOException {
        System.out.println(repository.retrieveSecret(Secret.COINCHECK_API_SECRET));
        System.out.println(repository.retrieveSecret(Secret.COINCHECK_API_ACCESS_KEY));
    }

    @Test
    void retrieveSecretMock() throws IOException {
        Mockito.when(repository.retrieveSecret(any()))
                .thenReturn("test-secret");
        // Act
        String actual = repository.retrieveSecret(Secret.COINCHECK_API_SECRET);

        // Assert
        assertEquals(actual, "test-secret");
    }
}