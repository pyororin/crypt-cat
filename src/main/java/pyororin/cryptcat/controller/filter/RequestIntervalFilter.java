package pyororin.cryptcat.controller.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.value;

@Component
@Slf4j
public class RequestIntervalFilter extends OncePerRequestFilter {

    private final Map<String, Instant> lastRequestMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {
        var requestWrapper = new BufferedServletRequestWrapper(request);

        String key = getGroupFromRequestBody(requestWrapper) + requestWrapper.getRequestURI();
        Instant now = Instant.now();
        if (Duration.between(lastRequestMap.getOrDefault(key, Instant.ofEpochMilli(0)), now).getSeconds() >= 60) {
            // 前回のリクエストからの経過時間が1分以上の場合、処理を継続
            lastRequestMap.put(key, now);
            filterChain.doFilter(requestWrapper, response);
        } else {
            // 前回のリクエストからの経過時間が1分未満の場合、処理をスキップ
            log.info("{} {}", value("kind", "request-skip"), value("key", key));
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Request interval too short. Please try again later.");
        }
    }

    private String getGroupFromRequestBody(HttpServletRequest request) throws IOException {
        try (InputStreamReader inputStreamReader = new InputStreamReader(request.getInputStream())) {
            return objectMapper.readTree(inputStreamReader)
                    .get("group")
                    .asText();
        }
    }
}
