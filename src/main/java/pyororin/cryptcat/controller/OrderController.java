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

    @PostMapping("/order/strategy/{pair}")
    public ResponseEntity<String> strategy(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        tradeService.order(Pair.fromValue(pair), orderRequest);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/order/strategy/{pair}/split")
    public ResponseEntity<String> strategySplit(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        tradeService.order(Pair.fromValue(pair), orderRequest);
        return ResponseEntity.ok("OK");
    }
}