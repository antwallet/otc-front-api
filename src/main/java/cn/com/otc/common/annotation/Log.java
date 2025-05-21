package cn.com.otc.common.annotation;

import cn.com.otc.common.enums.ActionEnum;

import java.lang.annotation.*;

/**
 * @description:日志注解
 * @author: zhangliyan
 * @time: 2022/5/18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.METHOD})
@Documented
public @interface Log {

     String desc() default "";//日志描述

     ActionEnum action() default ActionEnum.QUERY;//操作行为query,save等
}
