package cn.com.otc.modular.sys.bean.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户提现手续费统计
 * </p>
 *
 * @author zhangliyan
 * @since 2024-04-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user_withdrawal_tj")
public class TUserWithdrawalTj implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "user_id", type = IdType.INPUT)
    private String userId;

    /**
     * 邀请用户提现TRX总手续费数量
     */
    @TableField("withdrawal_total_money")
    private String withdrawalTotalMoney;

    /**
     * 邀请用户提现USDT总手续费数量
     */
    @TableField("withdrawal_total_money_usdt")
    private String withdrawalTotalMoneyUsdt;

    /**
     * 分摊的邀请用户trx手续费
     */
    @TableField("withdrawal_share_money")
    private String withdrawalShareMoney;

    /**
     * 分摊的邀请用户usdt手续费
     */
    @TableField("withdrawal_share_money_usdt")
    private String withdrawalShareMoneyUsdt;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

}
