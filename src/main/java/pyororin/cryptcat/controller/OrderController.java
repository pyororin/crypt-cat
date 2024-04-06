package pyororin.cryptcat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.TradeService;

@RestController
@Slf4j
@RequiredArgsConstructor
public class OrderController {
    private final TradeService tradeService;

    @PostMapping("/order/buy/{pair}")
    public ResponseEntity<String> buy(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        tradeService.buy(Pair.fromValue(pair), orderRequest);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/order/sell/{pair}")
    public ResponseEntity<String> sell(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        tradeService.sell(Pair.fromValue(pair), orderRequest);
        return ResponseEntity.ok("OK");
    }
}