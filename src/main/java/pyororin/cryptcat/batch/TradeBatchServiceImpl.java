package pyororin.cryptcat.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.config.OrderStatus;
import pyororin.cryptcat.config.OrderTransactions;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;

import java.math.RoundingMode;
import java.time.Clock;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeBatchServiceImpl {
    private final Clock clock;
    private final OrderTransactions orderTransactions;
    private final CoinCheckRepository repository;

    public void balance() {
        var ticker = repository.retrieveTicker(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build());
        var balance = repository.retrieveBalance();
        var btcToJpy = balance.getBtc().multiply(ticker.getLast());
        var btcReservedToJpy = balance.getBtc_reserved().multiply(ticker.getLast());
        var jpyToBtc = balance.getJpy().divide(ticker.getLast(), 9, RoundingMode.HALF_EVEN);
        var jpyReservedToBtc = balance.getJpy_reserved().divide(ticker.getLast(), 9, RoundingMode.HALF_EVEN);
        log.info("{} {} {} {} {} {} {} {}",
                value("kind", "balance"),
                value("jpy", balance.getJpy()),
                value("jpy_reserved", balance.getJpy_reserved()),
                value("btc", balance.getBtc()),
                value("btc_reserved", balance.getBtc_reserved()),
                value("rate", ticker.getLast()),
                value("total_jpy", balance.getJpy().add(balance.getJpy_reserved()).add(btcToJpy).add(btcReservedToJpy).setScale(0, RoundingMode.HALF_EVEN)),
                value("total_btc", balance.getBtc().add(balance.getBtc_reserved()).add(jpyToBtc).add(jpyReservedToBtc).setScale(9, RoundingMode.HALF_EVEN))
        );
    }

    public void transactions() {
        var response = repository.retrieveOrdersTransactions().withinMinutes(clock, 10);
        var sumFunds = response.sumFunds();
        log.info("{} {} {} {} {} {} {} {} {}",
                value("kind", "transactions"),
                value("count", response.getData().size()),
                value("sell-count", response.findBySide("sell").size()),
                value("buy-count", response.findBySide("buy").size()),
                value("jpy", sumFunds.getJpy()),
                value("btc", sumFunds.getBtc()),
                value("rate", response.avgRate()),
                value("buy-rate", response.getRateBySide("buy")),
                value("sell-rate", response.getRateBySide("sell"))
        );
    }

    @Scheduled(cron = "0 0,10,20,30,40,50 * * * *")
    public void clearTransactions() {
        orderTransactions.getOrderTransactions().entrySet()
                .removeIf(stringOrderTransactionEntry ->
                        stringOrderTransactionEntry.getValue().isCreatedAtMoreThanMinutesAgo(10)
                                && stringOrderTransactionEntry.getValue().getOrderStatus() == OrderStatus.ORDERED);
        log.info("{} {}", value("kind", "clear-transactions"), value("transactions", orderTransactions));
    }
}
