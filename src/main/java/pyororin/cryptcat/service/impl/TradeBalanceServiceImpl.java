package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;

import java.math.RoundingMode;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "coincheck.actually", havingValue = "true")
public class TradeBalanceServiceImpl {
    private final CoinCheckRepository repository;

    @Scheduled(cron = "30 0 * * * *")
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
}
