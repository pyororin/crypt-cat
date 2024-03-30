package pyororin.cryptcat.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pyororin.cryptcat.controller.model.OrderRequest;

import static net.logstash.logback.argument.StructuredArguments.value;

@RestController
@Slf4j
public class OrderController {

    @PostMapping("/order/buy")
    public ResponseEntity<String> buy(@RequestBody OrderRequest orderRequest) {
        log.info("{} {} {}",
                value("kind", "alert"),
                value("alert_type", "buy"),
                value("reason", orderRequest.getReason()));
        // TODO: 売買処理を行う
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/order/sell")
    public ResponseEntity<String> sell(@RequestBody OrderRequest orderRequest) {
        log.info("{} {} {}",
                value("kind", "alert"),
                value("alert_type", "sell"),
                value("reason", orderRequest.getReason()));
        // TODO: 売買処理を行う
        return ResponseEntity.ok("OK");
    }
}


