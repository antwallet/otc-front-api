package cn.com.otc.common.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2023/3/15
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoRepeatSubmit {

  /**
   * 过期时长（毫秒）
   *
   * @return
   */
  long expire() default 500;

}