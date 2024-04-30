package pyororin.cryptcat.repository;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.value;


@Aspect
@Component
@Slf4j
public class WaitAspects {

    @Before("execution(public * pyororin.cryptcat.repository.impl.*CheckRepositoryImpl.*(..)) && @annotation(pyororin.cryptcat.repository.BeforeWait)")
    public void sleepForBeforeApi() {
        try {
            log.info("{} {}", value("kind", "sleep"), value("value", 100));
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
