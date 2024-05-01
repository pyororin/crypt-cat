package pyororin.cryptcat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pyororin.cryptcat.controller.filter.RequestIntervalFilter;
import pyororin.cryptcat.service.IPCheckService;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {
    private final IPCheckService ipCheckService;

    @Bean
    public FilterRegistrationBean<RequestIntervalFilter> customFilterRegistration() {
        FilterRegistrationBean<RequestIntervalFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestIntervalFilter(ipCheckService));
        registration.addUrlPatterns("*");
        return registration;
    }
}