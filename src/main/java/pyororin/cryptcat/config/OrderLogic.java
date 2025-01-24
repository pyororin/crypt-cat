package pyororin.cryptcat.config;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum OrderLogic {
    HIGH(1),
    MEDIUM(2),
    LOW(5),
    EVEN(3);

    private final int cancelDelayMinutes;

    OrderLogic(int cancelDelayMinutes) {
        this.cancelDelayMinutes = cancelDelayMinutes;
    }
}
