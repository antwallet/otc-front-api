package cn.com.otc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RepackageRangeEnum {
    NEW("0","新用户"),
    ALL("1","所有人");

    private final String code;
    private final String desc;
}
