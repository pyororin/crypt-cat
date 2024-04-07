package pyororin.cryptcat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import pyororin.cryptcat.controller.filter.RequestIntervalFilter;
import pyororin.cryptcat.service.IPCheckService;
import pyororin.cryptcat.service.RequestIntervalService;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {
    private final IPCheckService ipCheckService;
    private final RequestIntervalService requestIntervalService;

    @Bean
    @Primary
    public FilterRegistrationBean<RequestIntervalFilter> customFilterRegistration() {
        FilterRegistrationBean<RequestIntervalFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestIntervalFilter(ipCheckService, requestIntervalService));
        registration.addUrlPatterns("/order/*");
        return registration;
    }
}