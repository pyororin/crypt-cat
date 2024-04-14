package pyororin.cryptcat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.TradeJpyFixService;
import pyororin.cryptcat.service.TradeService;

@RestController
@Slf4j
@RequiredArgsConstructor
public class OrderController {
    private final TradeService tradeService;
    private final TradeJpyFixService tradejpyFixService;

    @PostMapping("/order/strategy/{pair}")
    public ResponseEntity<String> strategy(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        tradeService.order(Pair.fromValue(pair), orderRequest);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/order/strategy/{pair}/split")
    public ResponseEntity<String> strategySplit(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        tradeService.orderSplit(Pair.fromValue(pair), orderRequest);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/order/jpyfix/{pair}")
    public ResponseEntity<String> jpyFix(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        tradejpyFixService.orderSplit(Pair.fromValue(pair), orderRequest);
        return ResponseEntity.ok("OK");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException() {
        return ResponseEntity.ok("NG");
    }
}