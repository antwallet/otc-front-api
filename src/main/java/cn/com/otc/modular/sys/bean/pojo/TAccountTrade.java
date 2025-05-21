package cn.com.otc.modular.sys.bean.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 账户交易表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_account_trade")
public class TAccountTrade implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账户ID
     */
    @TableField("account_id")
    private String accountId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 交易订单
     */
    @TableField("trade_no")
    private String tradeNo;

    /**
     * 交易金额
     */
    @TableField("money")
    private String money;

    /**交易类型 0、充值 1,提现 2,收款 3,发红包 4,收红包5、收红包 6、退红包 7、系统赠送
     8、系统扣除 9、提现失败退回 10、邀请返现 11、购买会员 12、购买会员失败返回
     13、购买能量 14、日榜收益 15、总榜收益 16、收回红包
     17、退未领完活动红包 18、退未发放榜单金额 19、扣除日榜发放金额 20、扣除总榜发放金额 21、分享红包发包扣除 22、退出（频道/群组）23、付款**/
    @TableField("trade_type")
    private Integer tradeType;

    @TableField("left_amount")
    private String leftAmount;

    @TableField("account_type")
    private Integer accountType;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    /**
     * 全开次数
     */
    @TableField("full_open_count")
    private Integer fullOpenCount;


}
