package cn.com.otc.common.utils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description:返回前端页面的数据结果
 * 模块的返回数据结构 为了方便 swagger识别 增加了泛型T (result)
 * @author: zhangliyan
 * @time: 2022/5/12
 */
@ApiModel(value="数据返回对象", description="")
@Data
public class ResultResponse<T>  implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "200代表成功，都代表失败（此处是自定义的数据code）")
    private Integer code;
    @ApiModelProperty(value = "返回消息")
    private String msg;
    @ApiModelProperty(value = "返回数据")
    private T data;//返回的数据


    private static final int SUCCESS_CODE= 200;
    private static final String SUCCESS_MSG= "成功";
    private static final int FAIL_CODE= 500;
    private static final String FAIL_MSG= "未知异常，请联系管理员";


    private static <T> ResultResponse<T> resultResponse(Integer code, String msg,T data) {
        ResultResponse<T> apiResult = new ResultResponse<>();
        apiResult.setCode(code);
        apiResult.setMsg(msg);
        apiResult.setData(data);
        return apiResult;
    }
    public static <T> ResultResponse<T> success(){
        return resultResponse(SUCCESS_CODE,SUCCESS_MSG,null);
    }

    public static <T> ResultResponse<T> success(T data){
        return resultResponse(SUCCESS_CODE,SUCCESS_MSG,data);
    }

    public static <T> ResultResponse<T> success(T data,String msg){
        return resultResponse(SUCCESS_CODE,msg,data);
    }

    public static <T> ResultResponse<T> failed(){
        return resultResponse(FAIL_CODE,FAIL_MSG,null);
    }

    public static <T> ResultResponse<T> failed(int code,String msg){
        return resultResponse(code,msg,null);
    }

    public static <T> ResultResponse<T> failed(int code,String msg,T data){
        return resultResponse(code,msg,data);
    }


}
