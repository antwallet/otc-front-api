package cn.com.otc.test;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/5/16
 */
public class TestDemo {

    public static void main(String[] args) {
//        BigDecimal total_amount = new BigDecimal("20");
//        BigDecimal total_amount_1 = new BigDecimal("30");
//        System.out.println(total_amount.compareTo(total_amount_1));
//        BigDecimal amount = new BigDecimal("0.000005");
//        total_amount = total_amount.add(amount);
//        System.out.println(total_amount);
        /*BigDecimal value = new BigDecimal("0.2");
        BigDecimal hundredPercent = new BigDecimal("1");
        BigDecimal result = value.multiply(hundredPercent);
        System.out.println("Result: " + result);*/

        /*DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        *//*long createTime = LocalDateTime.parse("2024-05-08 02:10:18",formatter).atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
        long expireTime = LocalDateTime.parse("2024-05-08 02:40:18",formatter).atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
        System.out.println(createTime+"/////"+expireTime);*//*
        BigDecimal amount = new BigDecimal("560");
        BigDecimal money = new BigDecimal("99");
        System.out.println(amount.divide(money));*/


    }
    @Test
    public void testExport(){
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date()));

    }

    @Test
    public void testImport(){
    }
}
