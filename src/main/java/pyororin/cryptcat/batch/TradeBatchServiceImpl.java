package pyororin.cryptcat.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.config.OrderStatus;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.FirestoreRepository;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;

import java.math.RoundingMode;
import java.time.Clock;
import java.util.concurrent.ExecutionException;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeBatchServiceImpl {
    private final Clock clock;
    private final CoinCheckRepository repository;
    private final FirestoreRepository firestore;

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

    public void transactions(int minutes) {
        var response = repository.retrieveOrdersTransactions().withinMinutes(clock, minutes).aggregateByRate();
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

    public void clearTransactions(int minutes) throws ExecutionException, InterruptedException {
        firestore.getAll().forEach((documentId, orderTransaction) -> {
            if (orderTransaction.isCreatedAtMoreThanMinutesAgo(minutes) && orderTransaction.getOrderStatus() == OrderStatus.ORDERED) {
                log.info("{} {}", value("kind", "clear-transaction"), value("transaction", orderTransaction));
                firestore.remove(documentId);
            }
        });
    }
}
