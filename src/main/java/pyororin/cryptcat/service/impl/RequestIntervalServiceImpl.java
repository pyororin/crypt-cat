package pyororin.cryptcat.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pyororin.cryptcat.service.RequestIntervalService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.value;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestIntervalServiceImpl implements RequestIntervalService {
    @Value("${skip.range.chart}")
    long skipRangeChart;

    private final Map<String, Instant> lastRequestMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean shouldNotProcessRequest(HttpServletRequest request) throws IOException {
        String key = getValueFromRequestBody(request, "group");
        long timePassed = calculateTimePassed(key);
        long necessaryCoolDown = TimeUnit.MINUTES.toSeconds(Long.parseLong(getValueFromRequestBody(request, "range")) * skipRangeChart);

        if (timePassed >= necessaryCoolDown) {
            lastRequestMap.put(key, Instant.now());
            return false; // 処理を継続する
        } else {
            log.info("{} {} 前回から{}秒経過 {}秒必要です",
                    value("kind", "request-skip"),
                    value("key", key),
                    value("time_passed", timePassed),
                    value("cool_down", necessaryCoolDown));
            return true; // 処理をスキップする
        }
    }

    private long calculateTimePassed(String key) {
        Instant lastRequestTime = lastRequestMap.getOrDefault(key, Instant.ofEpochMilli(0));
        return Duration.between(lastRequestTime, Instant.now()).getSeconds();
    }

    private String getValueFromRequestBody(HttpServletRequest request, String bodyParameter) throws IOException {
        try (var inputStreamReader = new InputStreamReader(request.getInputStream())) {
            return objectMapper.readTree(inputStreamReader)
                    .get(bodyParameter)
                    .asText();
        }
    }
}