package cn.com.otc.common.enums;

/**
 * @description:导出字段对齐方式枚举
 * @author: zhangliyan
 * @time: 2022/5/26
 */
public enum ExportAlignEnum {
    AUTO(0), LEFT(1), CENTER(2), RIGHT(3);
    private final int value;

    ExportAlignEnum(int value)
    {
        this.value = value;
    }

    public int value()
    {
        return this.value;
    }
}
