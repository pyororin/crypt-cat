package pyororin.cryptcat.repository.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Builder
@ToString
public class CoinCheckTransactionsResponse {
    private boolean success;
    private List<Data> data;
    private Pagination pagination;

    @lombok.Data()
    @Builder
    @ToString
    public static class Pagination {
        private long limit;
        private String order;
        @JsonProperty("starting_after")
        private long startingAfter;
        @JsonProperty("ending_before")
        private long endingBefore;
    }

    @lombok.Data
    @Builder
    @ToString
    public static class Data {
        private long id;
        @JsonProperty("order_id")
        private long orderId;
        @JsonProperty("created_at")
        private Instant createdAt;
        private Funds funds;
        private String pair;
        private BigDecimal rate;
        @JsonProperty("fee_currency")
        private String feeCurrency;
        private BigDecimal fee;
        private String liquidity;
        private String side;
    }

    @lombok.Data()
    @Builder
    @ToString
    public static class Funds {
        private BigDecimal btc;
        private BigDecimal jpy;
    }

    public List<Data> findOrdersWithinMinutes(Clock clock, int minutes) {
        return Objects.isNull(data) ? List.of() : data.stream()
                .filter(data -> data.getCreatedAt().isAfter(clock.instant().minus(minutes, ChronoUnit.MINUTES)))
                .collect(Collectors.toList());
    }

    public List<Data> findOrdersWithinMinutes(Clock clock, int minutes, String side) {
        return Objects.isNull(data) ? List.of() : data.stream()
                .filter(data -> data.getCreatedAt().isAfter(clock.instant().minus(minutes, ChronoUnit.MINUTES)))
                .filter(data -> data.getSide().equals(side))
                .collect(Collectors.toList());
    }

    public Funds sumFunds() {
        return Funds.builder().btc(this.data.stream()
                        .filter(data -> Objects.nonNull(data.getFunds()) && Objects.nonNull(data.getFunds().getBtc()))
                        .map(data -> data.getFunds().getBtc())
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .jpy(this.data.stream()
                        .filter(data -> Objects.nonNull(data.getFunds()) && Objects.nonNull(data.getFunds().getJpy()))
                        .map(data -> data.getFunds().getJpy())
                        .reduce(BigDecimal.ZERO, BigDecimal::add)).build();
    }

    public BigDecimal sumFundsToBtc() {
        return this.data.stream()
                .filter(data -> Objects.nonNull(data.getFunds()) && Objects.nonNull(data.getFunds().getBtc()))
                .map(data -> data.getFunds().getBtc())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(this.data.stream()
                        .filter(data -> Objects.nonNull(data.getFunds()) && Objects.nonNull(data.getFunds().getJpy()))
                        .map(data -> data.getFunds().getJpy().divide(data.rate, 9, RoundingMode.HALF_EVEN))
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal sumFundsToJpy() {
        return this.data.stream()
                .filter(data -> Objects.nonNull(data.getFunds()) && Objects.nonNull(data.getFunds().getJpy()))
                .map(data -> data.getFunds().getJpy())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(this.data.stream()
                        .filter(data -> Objects.nonNull(data.getFunds()) && Objects.nonNull(data.getFunds().getBtc()))
                        .map(data -> data.getFunds().getBtc().multiply(data.rate))
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
