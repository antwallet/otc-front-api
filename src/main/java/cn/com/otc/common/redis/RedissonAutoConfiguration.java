package cn.com.otc.common.redis;


import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @Date 2021/8/17 15:32
 * @Author zhangliyan
 **/
@Configuration
public class RedissonAutoConfiguration {

    @Value("${spring.redisson.address}")
    private String addressUrl;
    @Value("${spring.redisson.password}")
    private String password;
    @Value("${spring.redisson.timeout}")
    private int timeout;
    @Value("${spring.redisson.connectionPoolSize}")
    private int connectionPoolSize;
    @Value("${spring.redisson.connectionMinimumIdleSize}")
    private int connectionMinimumIdleSize;
    @Value("${spring.redisson.connectTimeout}")
    private int connectTimeout;
    @Value("${spring.redisson.pingConnectionInterval}")
    private int pingConnectionInterval;

    /**
     * @return org.redisson.api.RedissonClient
     * @Author huangwb
     * @Description
     * @Date 2020/3/19 22:54
     * @Param []
     **/
    @Bean
    public RedissonClient getRedisson() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(addressUrl)
                .setRetryInterval(5000)
                .setTimeout(timeout)
                .setConnectionPoolSize(connectionPoolSize)
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setConnectTimeout(connectTimeout)
                .setPingConnectionInterval(pingConnectionInterval);
        if (StringUtils.isNotBlank(password)) {
            config.useSingleServer().setPassword(password);
        }
        return Redisson.create(config);
    }
}
