package cn.com.otc.modular.sys.bean.vo;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @Auther: 2024
 * @Date: 2024/10/25 11:18
 * @Description:
 */
@Data
public class TPaymentRecordsVo {

    @NotNull(message = "pageIndex不能为空")
    @Min(value = 1, message = "pageIndex不能小于1")
    private Integer pageIndex;
    @NotNull(message = "pageSize不能为空")
    @Min(value = 1, message = "pageSize不能小于1")
    private Integer pageSize;
    @NotNull(message = "tronTransId不能为空")
    private String tronTransId;
    private String tgId;
}
