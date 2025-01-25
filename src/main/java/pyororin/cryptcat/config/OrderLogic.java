package pyororin.cryptcat.config;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum OrderLogic {
    HIGH(1),
    MEDIUM(1),
    LOW(1),
    EVEN(1);

    private final int cancelDelayMinutes;

    OrderLogic(int cancelDelayMinutes) {
        this.cancelDelayMinutes = cancelDelayMinutes;
    }
}
