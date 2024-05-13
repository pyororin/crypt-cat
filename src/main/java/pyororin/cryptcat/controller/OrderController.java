package pyororin.cryptcat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pyororin.cryptcat.batch.TradeBatchServiceImpl;
import pyororin.cryptcat.controller.model.OrderRequest;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.TradeService;

@RestController
@Slf4j
@RequiredArgsConstructor
public class OrderController {
    private final TradeService tradeBtcFixServiceImpl;
    private final TradeService tradeJpyFixServiceImpl;
    private final TradeService tradeJpyFixServiceV2Impl;
    private final TradeService tradeJpyFixServiceV3Impl;
    private final TradeBatchServiceImpl tradeBatchServiceImpl;

    @PostMapping("/order/btcfix/{pair}")
    public ResponseEntity<String> strategySplit(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        tradeBtcFixServiceImpl.order(Pair.fromValue(pair), orderRequest);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/order/jpyfix/{pair}")
    public ResponseEntity<String> jpyFix(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        tradeJpyFixServiceImpl.order(Pair.fromValue(pair), orderRequest);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/v2/order/jpyfix/{pair}")
    public ResponseEntity<String> jpyFixV2(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        tradeJpyFixServiceV2Impl.order(Pair.fromValue(pair), orderRequest);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/v3/order/jpyfix/{pair}")
    public ResponseEntity<String> jpyFixV3(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        tradeJpyFixServiceV3Impl.order(Pair.fromValue(pair), orderRequest);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/transactions/")
    public ResponseEntity<String> transactions() {
        tradeBatchServiceImpl.transactions();
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/balance/")
    public ResponseEntity<String> balance() {
        tradeBatchServiceImpl.balance();
        return ResponseEntity.ok("OK");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException() {
        return ResponseEntity.internalServerError().build();
    }
}