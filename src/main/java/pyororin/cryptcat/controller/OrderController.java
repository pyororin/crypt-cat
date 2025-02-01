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

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static net.logstash.logback.argument.StructuredArguments.value;

@RestController
@Slf4j
@RequiredArgsConstructor
public class OrderController {
    private final TradeService tradeBtcFixServiceImpl;
    private final TradeService tradeJpyFixServiceImpl;
    private final TradeService tradeJpyFixServiceV2Impl;
    private final TradeService tradeJpyFixServiceV3Impl;
    private final TradeService tradeJpyFixServiceV4Impl;
    private final TradeService tradeJpyFixServiceV5Impl;
    private final TradeService tradeJpyFixBuyServiceV6Impl;
    private final TradeService tradeJpyFixSellServiceV6Impl;
    private final TradeService tradeAllInBuyServiceV2Impl;
    private final TradeService tradeAllInSellServiceV2Impl;
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

    @PostMapping("/v4/order/jpyfix/{pair}")
    public ResponseEntity<String> jpyFixV4(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        tradeJpyFixServiceV4Impl.order(Pair.fromValue(pair), orderRequest);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/v5/order/jpyfix/{pair}")
    public ResponseEntity<String> jpyFixV5(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        tradeJpyFixServiceV5Impl.order(Pair.fromValue(pair), orderRequest);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/v6/order/jpyfix/{pair}")
    public ResponseEntity<String> jpyFixV6(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        if (orderRequest.isBuy()) {
            tradeJpyFixBuyServiceV6Impl.order(Pair.fromValue(pair), orderRequest);
        } else {
            tradeJpyFixSellServiceV6Impl.order(Pair.fromValue(pair), orderRequest);
        }
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/v2/order/allin/{pair}")
    public ResponseEntity<String> allinV2(@PathVariable String pair, @RequestBody @Validated OrderRequest orderRequest) {
        if (orderRequest.isBuy()) {
            tradeAllInBuyServiceV2Impl.order(Pair.fromValue(pair), orderRequest);
        } else {
            tradeAllInSellServiceV2Impl.order(Pair.fromValue(pair), orderRequest);
        }
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/transactions/{minutes}")
    public ResponseEntity<String> transactions(@PathVariable int minutes) {
        tradeBatchServiceImpl.transactions(minutes);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/balance/")
    public ResponseEntity<String> balance() {
        tradeBatchServiceImpl.balance();
        return ResponseEntity.ok("OK");
    }

    @DeleteMapping("/transaction/{minutes}")
    public ResponseEntity<String> clearTransactions(@PathVariable int minutes) throws ExecutionException, InterruptedException {
        tradeBatchServiceImpl.clearTransactions(minutes);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/retry/sell")
    public ResponseEntity<String> retryCell() {
        tradeBatchServiceImpl.retrySell();
        return ResponseEntity.ok("OK");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("{} {} {}", value("kind", "exception"),
                value("message", ex.getMessage()),
                value("stack-traces", Arrays.toString(ex.getStackTrace())));
        return ResponseEntity.internalServerError().build();
    }
}
