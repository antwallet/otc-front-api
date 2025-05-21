/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package cn.com.otc.common.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis-plus配置
 *
 * @author Mark sunlightcs@gmail.com
 */
@Configuration
public class MybatisPlusConfig {
    /*@Autowired
    private DefaultNumConfig defaultNumConfig;*/

    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInterceptor.setLimit(-1);
        return paginationInterceptor;
    }

//    @Bean
//    public MybatisPlusInterceptor mybatisPlusInterceptor() {
//        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
//        PaginationInnerInterceptor pa = new PaginationInnerInterceptor(DbType.MYSQL);
//        pa.setMaxLimit(Long.valueOf(defaultNumConfig.getMyBatisMaxLimit()));
//        interceptor.addInnerInterceptor(pa);
//        return interceptor;
//    }
//
//    @Bean
//    public ConfigurationCustomizer configurationCustomizer() {
//        return configuration -> configuration.setUseDeprecatedExecutor(false);
//    }

}
