package pyororin.cryptcat.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.service.IPCheckService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.value;

@Service
@Slf4j
@RequiredArgsConstructor
public class IPCheckServiceImpl implements IPCheckService {
    @Value("${allowed.ips}")
    private String allowedIPsConfig;

    private Set<String> allowedIPs;

    @Override
    public boolean isNotAllowedIPAddress(HttpServletRequest request) {
        if (allowedIPs == null) {
            allowedIPs = new HashSet<>(Arrays.asList(allowedIPsConfig.split(",")));
        }

        if (allowedIPs.contains(request.getHeader("x-forwarded-for"))) {
            return false;
        } else {
            log.warn("{} {}",
                    value("kind", "request-remote-ip-denied"),
                    value("remote-ip", request.getHeader("x-forwarded-for")));
            return true;
        }
    }
}
