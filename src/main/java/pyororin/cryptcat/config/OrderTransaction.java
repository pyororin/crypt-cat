package pyororin.cryptcat.config;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import pyororin.cryptcat.repository.model.OrderType;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Builder
@Data
@ToString
public class OrderTransaction {
    private long orderId;
    private String createdAt;
    private OrderType orderType;
    private OrderStatus orderStatus;

    public boolean isSell() {
        return OrderType.SELL == this.orderType;
    }

    public boolean isBuy() {
        return OrderType.BUY == this.orderType;
    }

    public boolean isBuySkip() {
        return (OrderType.BUY == this.orderType && OrderStatus.ORDERED == this.orderStatus)
                || (OrderType.SELL == this.orderType);
    }

    public boolean isSellSkip() {
        return (OrderType.SELL == this.orderType && OrderStatus.ORDERED == this.orderStatus)
                || (OrderType.BUY == this.orderType);
    }

    public boolean isCreatedAtMoreThanMinutesAgo(int minutes) {
        return Duration.between(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(this.createdAt)), Instant.now()).toMinutes() > minutes;
    }
}
