package cn.com.otc.common.annotation.export;

import cn.com.otc.common.enums.ExportAlignEnum;
import cn.com.otc.common.enums.ExportColumnTypeEnum;
import cn.com.otc.common.enums.ExportTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:允许导出字段注解
 * @author: zhangliyan
 * @time: 2022/5/26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnableExportField {
    /**设置头部列名*/
    String colName();
    /**设置列宽度*/
    int colWidth() default  100;
    /**设置排序*/
    int sort() default Integer.MAX_VALUE;
    /**设置日期格式*/
    String dateFormat() default "";
    /**是否导出数据,应对需求:有时我们需要导出一份模板,这是标题需要但内容需要用户手工填写.*/
    boolean isExport() default true;

    /**
     * 当值为空时,字段的默认值
     */
    String defaultValue() default "";

    /**
     * 导出类型（0数字 1字符串）
     */
    ExportColumnTypeEnum cellType() default ExportColumnTypeEnum.STRING;

    /**
     * 导出字段对齐方式（0：默认；1：靠左；2：居中；3：靠右）
     */
    ExportAlignEnum align() default ExportAlignEnum.AUTO;

    /**
     * 字段类型（0：导出导入；1：仅导出；2：仅导入）
     */
    ExportTypeEnum type() default ExportTypeEnum.ALL;

}


