package pyororin.cryptcat.repository.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import pyororin.cryptcat.config.OrderStatus;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Builder
@Data
@ToString
public class OrderTransaction {
    private Long orderId;
    private String createdAt;
    private OrderType orderType;
    private OrderStatus orderStatus;

    public boolean isSell() {
        return OrderType.SELL == this.orderType;
    }

    public boolean isBuy() {
        return OrderType.BUY == this.orderType;
    }

    public boolean isOrdered() {
        return OrderStatus.ORDERED == this.orderStatus;
    }

    public boolean isCancel() {
        return OrderStatus.CANCEL == this.orderStatus;
    }

    public boolean isBuySkip() {
//        return (isBuy() && isOrdered()) || isSell();
        return isSell() && isCancel();
    }

    public boolean isSellSkip() {
//        return (isSell() && isOrdered()) || isBuy();
        return isBuy() && isCancel();
    }

    public boolean isCreatedAtMoreThanMinutesAgo(int minutes) {
        return Duration.between(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(this.createdAt)), Instant.now()).toMinutes() > minutes;
    }
}
