/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package cn.com.otc.common.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Filter配置
 *
 * @author Mark sunlightcs@gmail.com
 */
@Configuration
public class FilterConfig {

   @Bean
    public FilterRegistrationBean filterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new Cosfilter());
        registration.setName("cosfilter");//过滤器名称
        registration.addUrlPatterns("/*");//过滤所有路径
        registration.setOrder(1);//优先级，最顶级
        return registration;
    }
}
