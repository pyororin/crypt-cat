package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.repository.CoinCheckRepository;
import pyororin.cryptcat.repository.model.CoinCheckRequest;
import pyororin.cryptcat.repository.model.Pair;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeBalanceServiceImpl {
    private final CoinCheckRepository repository;

    @Scheduled(cron = "0 0 * * * *")
    public void balance() {
        var ticker = repository.retrieveTicker(CoinCheckRequest.builder().pair(Pair.BTC_JPY).build());
        var balance = repository.retrieveBalance();
        var btcToYen = balance.getBtc().multiply(ticker.getLast());
        log.info("{} {} {} {} {}",
                value("kind", "balance"),
                value("jpy", balance.getJpy()),
                value("btc", balance.getBtc()),
                value("rate", ticker.getLast()),
                value("total", balance.getJpy().add(btcToYen))
        );
    }
}
