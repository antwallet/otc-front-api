package cn.com.otc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCodeEnum {
    INTERFACE_ERROR(501,"接口异常"),
    SERVER_ERROR(500,"服务器错误"),
    PARAM_ERROR(100,"参数错误");

    final int code;
    final String msg;


}
