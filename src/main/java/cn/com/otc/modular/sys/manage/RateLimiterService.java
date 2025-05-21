package cn.com.otc.modular.sys.manage;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> rateLimiterScript;

    public RateLimiterService(StringRedisTemplate redisTemplate) throws IOException {
        this.redisTemplate = redisTemplate;

        // 读取 Lua 脚本文件
        ClassPathResource resource = new ClassPathResource("rate_limiter.lua");
        String scriptSource = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        // 创建 RedisScript 实例
        this.rateLimiterScript = new DefaultRedisScript<>(scriptSource, Long.class);
    }

    // isAllowed 方法保持不变
    public boolean isAllowed(String key, int limit, int expireTime) {
        List<String> keys = Collections.singletonList(key);
        Long result = redisTemplate.execute(
                rateLimiterScript,
                keys,
                String.valueOf(limit),
                String.valueOf(expireTime)
        );
        return result != null && result == 1;
    }
}
