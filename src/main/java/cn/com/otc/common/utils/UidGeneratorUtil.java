package cn.com.otc.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * @description:生成唯一ID
 * @author: zhangliyan
 * @time: 2024/2/3
 */
public class UidGeneratorUtil {

  private static Snowflake snowflake;

  public static String genId () {
    if (null == snowflake) {
      synchronized (UidGeneratorUtil.class) {
        if (null == snowflake) {
          snowflake = IdUtil.getSnowflake(2, 3);
        }
      }
    }
    long id = snowflake.nextId();
    return String.valueOf(id);
  }

  public static void main(String[] args) {
    Snowflake snowflake = IdUtil.getSnowflake(2, 3);
    long snowflakeId = snowflake.nextId();
    System.out.println("snowflake生成的id:"+snowflakeId);

    String nanoId = IdUtil.nanoId(19);
    System.out.println("nanoId生成的id:"+nanoId);
  }

}
