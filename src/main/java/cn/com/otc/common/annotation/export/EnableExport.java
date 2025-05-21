package cn.com.otc.common.annotation.export;

import java.lang.annotation.*;

/**
 * @description:允许导出注解
 * @author: zhangliyan
 * @time: 2022/5/26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EnableExport {
    String fileName();//导出的文件名

    String sheetName();//工作薄名称
}
