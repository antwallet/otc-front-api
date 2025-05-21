package cn.com.otc.common.enums;

/**
 * @description:导出类型枚举
 * @author: zhangliyan
 * @time: 2022/5/26
 */
public enum ExportColumnTypeEnum {
    NUMERIC(0), STRING(1), IMAGE(2);
    private final int value;

    ExportColumnTypeEnum(int value)
    {
        this.value = value;
    }

    public int value()
    {
        return this.value;
    }
}
