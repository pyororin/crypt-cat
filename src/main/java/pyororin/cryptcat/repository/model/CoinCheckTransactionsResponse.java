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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public CoinCheckTransactionsResponse withinMinutes(Clock clock, int minutes) {
        var now = clock.instant();
        this.data = Objects.isNull(data) ? List.of() : data.stream()
                .filter(data -> {
                    Instant createdAt = data.getCreatedAt();
                    return !createdAt.isBefore(now.truncatedTo(ChronoUnit.MINUTES).minus(minutes, ChronoUnit.MINUTES))
                            && createdAt.isBefore(now.truncatedTo(ChronoUnit.MINUTES));
                })
                .collect(Collectors.toList());
        return this;
    }

    public List<Data> findBySide(String side) {
        return Objects.isNull(data) ? List.of() : data.stream()
                .filter(data -> data.getSide().equals(side))
                .collect(Collectors.toList());
    }

    public BigDecimal avgRate() {
        // ユニークなレートのリストを取得し重複を省く
        var uniqueRates = this.data.stream()
                .map(Data::getRate)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // ユニークなレートの数で合計を割って平均値を求める
        if (uniqueRates.isEmpty()) {
            return BigDecimal.ZERO;
        } else {
            return uniqueRates.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(uniqueRates.size()), 2, RoundingMode.HALF_EVEN);
        }
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

    public BigDecimal getRateBySide(String side) {

        // ユニークなレートのリストを取得し重複を省く
        var uniqueRates = this.data.stream()
                .filter(data -> data.getSide().equals(side))
                .distinct()
                .map(Data::getRate)
                .toList();

        // ユニークなレートの数で合計を割って平均値を求める
        if (uniqueRates.isEmpty()) {
            return BigDecimal.ZERO;
        } else {
            return uniqueRates.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(uniqueRates.size()), 2, RoundingMode.HALF_EVEN);
        }
    }

    public CoinCheckTransactionsResponse aggregateByRate() {
        // Group by rate with tolerance of ±10
        Map<Integer, List<Data>> groupedByRate = this.data.stream()
                .collect(Collectors.groupingBy(
                        data -> data.getRate().subtract(new BigDecimal(10)).divide(new BigDecimal(20), RoundingMode.DOWN).intValue()
                ));

        // Aggregate the grouped data
        List<Data> aggregatedList = new ArrayList<>();
        for (List<Data> group : groupedByRate.values()) {
            Data aggregatedData = group.stream().reduce((data1, data2) -> Data.builder()
                    .id(data1.getId()) // or some logic to decide which id to keep
                    .orderId(data1.getOrderId()) // similarly, decide which orderId to keep
                    .createdAt(data1.getCreatedAt()) // similarly, decide which createdAt to keep
                    .funds(Funds.builder()
                            .btc(data1.getFunds().getBtc().add(data2.getFunds().getBtc()))
                            .jpy(data1.getFunds().getJpy().add(data2.getFunds().getJpy()))
                            .build())
                    .pair(data1.getPair())
                    .rate(data1.getRate()) // or some logic to decide which rate to keep
                    .feeCurrency(data1.getFeeCurrency())
                    .fee(data1.getFee().add(data2.getFee()))
                    .liquidity(data1.getLiquidity())
                    .side(data1.getSide())
                    .build()).get();
            aggregatedList.add(aggregatedData);
        }

        // Update the data field with the aggregated list
        this.data = aggregatedList;
        return this;
    }
}
