package pyororin.cryptcat.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Builder
@Data
@ToString
public class CoinCheckOpensOrdersResponse {
    private boolean success;
    private List<Order> orders;

    @Builder
    @Data
    @ToString
    public static class Order {

        private long id;
        @JsonProperty("order_type")
        private String orderType;
        private BigDecimal rate;
        private String pair;
        @JsonProperty("pending_amount")
        private BigDecimal pendingAmount;
        @JsonProperty("pending_market_buy_amount")
        private BigDecimal pendingMarketBuyAmount;
        @JsonProperty("stop_loss_rate")
        private BigDecimal stopLossRate;
        @JsonProperty("created_at")
        private Instant createdAt;
    }

    public List<CoinCheckOpensOrdersResponse.Order> findOrdersOver24Hours(Clock clock) {
        return Objects.isNull(orders) ? List.of() : orders.stream()
                .filter(order -> order.getCreatedAt().isBefore(clock.instant().minus(24, ChronoUnit.HOURS)))
                .collect(Collectors.toList());
    }
}
