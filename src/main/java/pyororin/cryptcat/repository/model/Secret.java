package pyororin.cryptcat.repository.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Secret {
    COINCHECK_API_ACCESS_KEY("coincheck-api-access-key"),
    COINCHECK_API_SECRET("coincheck-api-secret");

    private final String value;
}
