package pyororin.cryptcat.controller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.filter.OncePerRequestFilter;
import pyororin.cryptcat.service.IPCheckService;
import pyororin.cryptcat.service.RequestIntervalService;

import java.io.IOException;


@Slf4j
@RequiredArgsConstructor
public class RequestIntervalFilter extends OncePerRequestFilter {
    private final IPCheckService ipCheckService;
    private final RequestIntervalService requestIntervalService;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws IOException, ServletException {
        var requestWrapper = new BufferedServletRequestWrapper(request);

        // リクエスト元のIPアドレスをチェック
        if (ipCheckService.isNotAllowedIPAddress(requestWrapper)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }
        // 前回からのリクエストからの経過時間を判定
        if (requestIntervalService.shouldNotProcessRequest(requestWrapper)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Request interval too short. Please try again later.");
            return;
        }
        filterChain.doFilter(requestWrapper, response);
    }
}
