package cn.com.otc.common.utils;

import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigInteger;
@Component
@Slf4j
public class CommonUtil {
    private static final String BASE62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_LINK_LENGTH = 20; // 控制短链接的长度
    @Resource
    private RedissonClient redissonClient;


    // Base62编码
    private String base62Encode(byte[] input) {
        BigInteger bigInt = new BigInteger(1, input);  // 将字节数组转换为一个大整数
        StringBuilder result = new StringBuilder();
        while (bigInt.compareTo(BigInteger.ZERO) > 0) {
            int remainder = bigInt.mod(BigInteger.valueOf(62)).intValue();
            result.append(BASE62.charAt(remainder));
            bigInt = bigInt.divide(BigInteger.valueOf(62));
        }
        return result.reverse().toString();  // 反转字符串，因为我们从最低位开始编码
    }
}
