package cn.com.otc.common.response;

import cn.com.otc.common.enums.ErrorCodeEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class ResponseEntity<T> {
    // Getter 和 Setter 方法
    private int code;       // 响应状态码
    private String message; // 响应提示信息
    private T data;         // 响应数据

    // 构造方法
    public ResponseEntity() {}

    public ResponseEntity(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 静态方法用于构建成功响应
    public static <T> ResponseEntity<T> success(T data) {
        return new ResponseEntity<>(0, "Success", data);
    }

    // 静态方法用于构建自定义成功响应
    public static <T> ResponseEntity<T> success(String message, T data) {
        return new ResponseEntity<>(0, message, data);
    }

    // 静态方法用于构建失败响应
    public static <T> ResponseEntity<T> failure(ErrorCodeEnum errorCodeEnum) {
        return new ResponseEntity<>(errorCodeEnum.getCode(), errorCodeEnum.getMsg(), null);
    }

    public static <T> ResponseEntity<T> failure(int code, String msg) {
        return new ResponseEntity<>(code, msg, null);
    }

    public static <T> ResponseEntity<T> failure(String msg) {
        return new ResponseEntity<>(-1,msg, null);
    }

}

