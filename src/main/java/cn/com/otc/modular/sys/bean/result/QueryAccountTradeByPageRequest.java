package cn.com.otc.modular.sys.bean.result;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 查询交易记录
 * @author binbin
 */
@Data
public class QueryAccountTradeByPageRequest {
    @NotNull(message = "pageIndex不能为空")
    @Min(value = 1, message = "pageIndex不能小于1")
    private Integer pageIndex;
    @NotNull(message = "pageSize不能为空")
    @Min(value = 1, message = "pageSize不能小于1")
    private Integer pageSize;
    @NotBlank(message = "accountId不能为空")
    private String accountId;
    @NotNull(message = "accountType不能为空")
    private Integer accountType;

    /**交易类型 充值
     0,提现 1,提现 2,收款 3,发红包 4,收红包5、收红包 6、退红包 7、系统赠送
     8、系统扣除 9、提现失败退回 10、邀请返现 11、购买会员 12、购买会员失败返回
    13、购买能量 14、日榜收益 15、总榜收益 16、收回红包
    17、退未领完活动红包 18、退未发放榜单金额 19、扣除日榜发放金额 20、扣除总榜发放金额 21、分享红包发包扣除 22、退出（频道/群组）23、付款**/
    private Integer tradeType;
}
