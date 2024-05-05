package pyororin.cryptcat.repository;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Aspect
@Component
@Slf4j
public class WaitAspects {

    @Around("execution(public * pyororin.cryptcat.repository.impl.*CheckRepositoryImpl.*(..)) && @annotation(pyororin.cryptcat.repository.BeforeWait)")
    public Object sleepForBeforeApi(ProceedingJoinPoint pjp) throws Throwable {
        // Rate Limit 6回/sec
        try {
            TimeUnit.MILLISECONDS.sleep(1000 / 6 / 2);
            var object = pjp.proceed();
            TimeUnit.MILLISECONDS.sleep(1000 / 6 / 2);
            return object;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
