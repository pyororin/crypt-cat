package pyororin.cryptcat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pyororin.cryptcat.controller.model.OrderRequest;

@RestController
public class OrderController {

    @PostMapping("/order/sell")
    public ResponseEntity<String> sell(@RequestBody OrderRequest orderRequest) {
        String reason = orderRequest.getReason();
        // 売買処理を行う
        return ResponseEntity.ok("Sell order received with reason: " + reason);
    }

    @PostMapping("/order/buy")
    public ResponseEntity<String> buy(@RequestBody OrderRequest orderRequest) {
        String reason = orderRequest.getReason();
        // 売買処理を行う
        return ResponseEntity.ok("Buy order received with reason: " + reason);
    }
}


