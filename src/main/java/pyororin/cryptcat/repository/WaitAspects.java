package pyororin.cryptcat.repository;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Aspect
@Component
@Slf4j
public class WaitAspects {

    @Before("execution(public * pyororin.cryptcat.repository.impl.*CheckRepositoryImpl.*(..)) && @annotation(pyororin.cryptcat.repository.AfterWait)")
    public void sleepForAfterApi() {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
