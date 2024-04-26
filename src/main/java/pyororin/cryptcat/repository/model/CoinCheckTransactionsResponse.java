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

@Data
@Builder
@ToString
public class CoinCheckTransactionsResponse {
    private boolean success;
    private List<Transaction> transactions;

    @Data
    @Builder
    @ToString
    public static class Transaction {
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

    @Data()
    @Builder
    @ToString
    public static class Funds {
        private BigDecimal btc;
        private BigDecimal jpy;
    }

    public List<CoinCheckTransactionsResponse.Transaction> findOrdersWithinHours(Clock clock) {
        return Objects.isNull(transactions) ? List.of() : transactions.stream()
                .filter(transaction -> transaction.getCreatedAt().isAfter(clock.instant().minus(1, ChronoUnit.HOURS)))
                .collect(Collectors.toList());
    }

    public Funds sumFunds(Clock clock) {
        BigDecimal totalBtc = this.findOrdersWithinHours(clock).stream()
                .filter(transaction -> Objects.nonNull(transaction.getFunds()) && Objects.nonNull(transaction.getFunds().getBtc()))
                .map(transaction -> transaction.getFunds().getBtc())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalJpy = this.findOrdersWithinHours(clock).stream()
                .filter(transaction -> Objects.nonNull(transaction.getFunds()) && Objects.nonNull(transaction.getFunds().getJpy()))
                .map(transaction -> transaction.getFunds().getJpy())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Funds.builder().btc(totalBtc).jpy(totalJpy).build();
    }
}
