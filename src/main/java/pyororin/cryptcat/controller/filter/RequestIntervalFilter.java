package pyororin.cryptcat.controller.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.value;

@Component
@Slf4j
public class RequestIntervalFilter extends OncePerRequestFilter {
    @Value("${skip.range.chart}")
    long skipRangeChart;

    @Value("${allowed.ips}")
    private String allowedIPsConfig;

    private Set<String> allowedIPs;

    private final Map<String, Instant> lastRequestMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {
        // リクエスト元のIPアドレスをチェック
        if (allowedIPs == null) {
            allowedIPs = new HashSet<>(Arrays.asList(allowedIPsConfig.split(",")));
        }

        if (!allowedIPs.contains(request.getHeader("x-forwarded-for"))) {
            log.warn("{} {}",
                    value("kind", "request-remote-ip-denied"),
                    value("remote-ip", request.getHeader("x-forwarded-for")));
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }
        // 前回からのリクエストからの経過時間を判定
        var requestWrapper = new BufferedServletRequestWrapper(request);

        String key = getGroupFromRequestBody(requestWrapper, "group") + requestWrapper.getRequestURI();
        long timePassed = Duration.between(lastRequestMap.getOrDefault(key, Instant.ofEpochMilli(0)), Instant.now()).getSeconds();
        long necessaryCoolDown = TimeUnit.MINUTES.toSeconds(Long.parseLong(getGroupFromRequestBody(requestWrapper, "range")) * skipRangeChart);

        if (timePassed >= necessaryCoolDown) {
            // 前回のリクエストからの経過時間が1分以上の場合、処理を継続
            lastRequestMap.put(key, Instant.now());
            filterChain.doFilter(requestWrapper, response);
        } else {
            // 前回のリクエストからの経過時間が1分未満の場合、処理をスキップ
            log.info("{} {} 前回から{}秒経過 {}秒必要です",
                    value("kind", "request-skip"),
                    value("key", key),
                    value("time_passed", timePassed),
                    value("cool_down", necessaryCoolDown));
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Request interval too short. Please try again later.");
        }
    }

    private String getGroupFromRequestBody(HttpServletRequest request, String bodyParameter) throws IOException {
        try (var inputStreamReader = new InputStreamReader(request.getInputStream())) {
            return objectMapper.readTree(inputStreamReader)
                    .get(bodyParameter)
                    .asText();
        }
    }
}
