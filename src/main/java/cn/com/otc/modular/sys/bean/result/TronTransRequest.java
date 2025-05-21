package cn.com.otc.modular.sys.bean.result;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 查询分享记录
 * @author xiaoyao
 */
@Data
public class TronTransRequest {
    /* 收款Id */
    private String tronTransId;
}
