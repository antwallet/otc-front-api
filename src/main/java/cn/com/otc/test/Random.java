package cn.com.otc.test;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Auther: 2024
 * @Date: 2024/7/9 13:57
 * @Description: 随机数测试
 */
public class Random {

    public static void main(String[] args) {
        BigDecimal min = new BigDecimal("0");
        BigDecimal max = new BigDecimal("20");
        // 创建Random对象
        java.util.Random random = new java.util.Random();
        // 生成随机数，范围在[min, max)
        BigDecimal randomBigDecimal = min.add(
                new BigDecimal(random.nextDouble())
                        .multiply(max.subtract(min))
        );
        // 设定小数点后保留的位数和舍弃模式
        BigDecimal result = randomBigDecimal.setScale(2, RoundingMode.DOWN);

        System.out.printf(result.toString());
    }
}
