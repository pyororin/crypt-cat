package pyororin.cryptcat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.TradeService;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(name = "coincheck.actually", havingValue = "true")
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {
    @Override
    public BigDecimal buy(Pair pair) {
        System.out.println("Executing buy operation in TradeServiceImpl...");
        return BigDecimal.valueOf(0);
        // buyの具体的な処理
    }

    @Override
    public BigDecimal sell(Pair pair) {
        System.out.println("Executing sell operation in TradeServiceImpl...");
        return BigDecimal.valueOf(0);
        // sellの具体的な処理
    }
}