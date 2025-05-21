package cn.com.otc.common.config.interceptor;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IpFilterConfig {
    @Bean
    public FilterRegistrationBean<IpRateLimiterFilter> ipRateLimiterFilter(IpRateLimiterFilter filter) {
        FilterRegistrationBean<IpRateLimiterFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/*"); // 应用到所有URL
        registrationBean.setOrder(1); // 设置过滤器顺序
        return registrationBean;
    }
}
