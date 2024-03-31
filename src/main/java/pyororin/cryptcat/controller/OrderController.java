package pyororin.cryptcat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.TradeService;

import static net.logstash.logback.argument.StructuredArguments.value;

@RestController
@Slf4j
@RequiredArgsConstructor
public class OrderController {
    private final TradeService tradeService;

    @PostMapping("/order/buy/{pair}")
    public ResponseEntity<String> buy(@PathVariable String pair, @RequestBody OrderRequest orderRequest) {
        log.info("{} {} {} {}",
                value("kind", "alert"),
                value("alert_type", "buy"),
                value("pair", pair),
                value("reason", orderRequest.getReason()));
        tradeService.buy(Pair.fromValue(pair));
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/order/sell/{pair}")
    public ResponseEntity<String> sell(@PathVariable String pair, @RequestBody OrderRequest orderRequest) {
        log.info("{} {} {} {}",
                value("kind", "alert"),
                value("alert_type", "sell"),
                value("pair", pair),
                value("reason", orderRequest.getReason()));
        tradeService.sell(Pair.fromValue(pair));
        return ResponseEntity.ok("OK");
    }
}