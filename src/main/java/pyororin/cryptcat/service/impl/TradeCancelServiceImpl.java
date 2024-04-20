package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.repository.CoinCheckRepository;

import java.time.Clock;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeCancelServiceImpl {
    private final Clock clock;
    private final CoinCheckRepository repository;

    @Scheduled(cron = "0 * * * * *")
    public void cancel() {
        repository.retrieveOpensOrders().findOrdersOver24Hours(clock).forEach(order -> {
            repository.exchangeCancel(order.getId());
            log.info("{} {} {}",
                    value("kind", "cancel"),
                    value("order_time", order.getCreatedAt()),
                    value("id", order.getId()));
        });
    }
}
