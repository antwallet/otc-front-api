package cn.com.otc.common.config;

import cn.com.otc.common.config.interceptor.ApiInterceptor;
import cn.com.otc.common.config.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private ApiInterceptor apiInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

/*        List<String> ignoreUrlsConfig = new ArrayList<>();
        ignoreUrlsConfig.add("/error");
        ignoreUrlsConfig.add("/login");
        ignoreUrlsConfig.add("/login.html");
        ignoreUrlsConfig.add("/swagger/**");
        ignoreUrlsConfig.add("/swagger-ui.html");
        ignoreUrlsConfig.add("/swagger-resources/**");
        ignoreUrlsConfig.add("/webjars/springfox-swagger-ui/**");
        ignoreUrlsConfig.add("/static/**");
        ignoreUrlsConfig.add("/favicon.ico");*/

        List<String> ignoreUrlsConfig = new ArrayList<>();
        ignoreUrlsConfig.add("/error");
        ignoreUrlsConfig.add("/login");
        ignoreUrlsConfig.add("/login.html");
        ignoreUrlsConfig.add("/swagger-ui.html");
        ignoreUrlsConfig.add("/swagger-resources/**");
        ignoreUrlsConfig.add("/v2/api-docs");
        ignoreUrlsConfig.add("/webjars/**");
        ignoreUrlsConfig.add("/swagger-ui/**");
        ignoreUrlsConfig.add("/configuration/**");
        ignoreUrlsConfig.add("/**/*.js");
        ignoreUrlsConfig.add("/**/*.css");
        ignoreUrlsConfig.add("/**/*.png");
        ignoreUrlsConfig.add("/**/*.ico");

        // 登录拦截器
        registry.addInterceptor(loginInterceptor)
                 .addPathPatterns("/**")
                .excludePathPatterns(ignoreUrlsConfig)
                .excludePathPatterns("/api/v1/**")
                .excludePathPatterns("/api/front/redpacket_manage/queryUserRecieveStatus")
                .excludePathPatterns("/api/front/auth/doLogin")
                .excludePathPatterns("/api/front/redpacket_manage/openBotOrMiniapp")
                .excludePathPatterns("/api/front/thirdPayment/indianPamentCallback")
                .excludePathPatterns("/api/front/thirdPayment/philippinesPamentCallback")
                .excludePathPatterns("/antwalletbot_oauth2_server/**");

        // api拦截器
        registry.addInterceptor(apiInterceptor)
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns(ignoreUrlsConfig)
                .excludePathPatterns("/api/front/redpacket_manage/queryUserRecieveStatus")
                .excludePathPatterns("/api/front/auth/doLogin")
                .excludePathPatterns("/api/front/redpacket_manage/openBotOrMiniapp")
                .excludePathPatterns("/api/front/thirdPayment/indianPamentCallback")
                .excludePathPatterns("/api/front/thirdPayment/philippinesPamentCallback")
                .excludePathPatterns("/antwalletbot_oauth2_server/**");
    }
}
