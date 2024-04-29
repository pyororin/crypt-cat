package pyororin.cryptcat.repository;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class WaitAspects {

    @After("execution(public * pyororin.cryptcat.repository.impl.CoinCheckRepositoryImpl.*(..)) && @annotation(pyororin.cryptcat.repository.AfterWait)")
    public void sleepForAfterApi() {
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
