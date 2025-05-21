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
 * 用户提现表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-04-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user_withdrawal")
public class TUserWithdrawal implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 提现ID
     */
    @TableField("wallet_id")
    private String walletId;

    /**
     * 提现用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 提现的钱包地址
     */
    @TableField("hex_address")
    private String hexAddress;

    /**
     * 账户类型 0,TRX 1,USDT
     */
    @TableField("account_type")
    private Integer accountType;

    /**
     * 链类型 0,tron 1,ton
     */
    @TableField("blockchain_type")
    private Integer blockchainType;

    /**
     * 提现数量
     */
    @TableField("money")
    private String money;

    /**
     * 实际提现数量
     */
    @TableField("withdrawal_money")
    private String withdrawalMoney;

    /**
     * 转账手续费
     */
    @TableField("withdrawal_rate")
    private String withdrawalRate;

    /**
     * 提现状态 0,未审核 1,审核通过,提现进行中 2,提现审核失败,3,审核通过,提现成功 4,审核通过,提现失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 提现txid
     */
    @TableField("withdrawal_txid")
    private String withdrawalTxid;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 提现手续费-区块链
     */
    @TableField("withdrawal_blockchain_fee")
    private String withdrawalBlockchainFee;

    /**
     * 银行名称
     */
    @TableField("bank_name")
    private String bankName;

    /**
     * 收款人姓名
     */
    @TableField("bank_card_name")
    private String bankCardName;

    /**
     * 收款人账号
     */
    @TableField("bank_card_account")
    private String bankCardAccount;
    @TableField("IFSC")
    private String IFSC;
    /**
     * 1-拼多多提现
     */
    @TableField("pin_duo_duo")
    private String pinDuoDuo;
    @TableField("philippines_withdrawal_type")
    private String philippinesWithdrawalType;
}
