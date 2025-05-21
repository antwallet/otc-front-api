package cn.com.otc.common.config;

import cn.com.otc.common.config.vo.MyDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/6/9
 */
@Configuration
@PropertySource({"classpath:application-druid.yml"})
public class MyDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix="spring.datasource.dynamic.datasource.master")
    public MyDataSource getMasterDataSourceConfig(){
       return new MyDataSource();
    }


    @Bean
    @ConfigurationProperties(prefix="spring.datasource.dynamic.datasource.slave")
    public MyDataSource getSlaveDataSourceConfig(){
        return new MyDataSource();
    }

}
