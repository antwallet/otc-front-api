package cn.com.otc.common.enums;

/**
 * @description:字段类型枚举
 * @author: zhangliyan
 * @time: 2022/5/26
 */
public enum ExportTypeEnum {
    ALL(0), EXPORT(1), IMPORT(2);
    private final int value;

    ExportTypeEnum(int value)
    {
        this.value = value;
    }

    public int value()
    {
        return this.value;
    }
}
