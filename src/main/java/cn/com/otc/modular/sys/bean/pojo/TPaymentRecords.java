package cn.com.otc.modular.sys.bean.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 收款表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-03-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_payment_records")
public class TPaymentRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * '收款ID'
     */
    @TableField(value = "tron_trans_id")
    private String tronTransId;
    /**
     * 付款ID'
     */
    @TableField(value = "payment_id")
    private String paymentId;

    /**
     * 收款用户ID
     */
    @TableField(value = "send_user_id")
    private String sendUserId;
    /**
     * 付款用户ID
     */
    @TableField(value = "payment_user_id")
    private String paymentUserId;

    /**
     * 付款金额
     */
    @TableField(value = "payment_money")
    private String paymentMoney;

    /**
     * 账户类型
     */
    @TableField(value = "account_type")
    private Integer accountType;

    /**
     * 付款状态 0,已付款 1,申述中 2,申述成功 3,申述失败 4、付款成功 5、订阅已过期
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 付款时间
     */
    @TableField(value = "chat_bot_name")
    private String chatBotName;
    /**
     * 付款时间
     */
    @TableField(value = "payment_time")
    private LocalDateTime paymentTime;


    /**
     * 申述过期时间
     */
    @TableField(value = "expiry_time")
    private LocalDateTime expiryTime;

    /**
     * 订阅过期时间
     */
    @TableField(value = "subscription_expiry_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime subscriptionExpiryTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
    /**
     * 付款用户的用户名
     */
    @TableField(exist = false)
    private String paymentUserName;

    /**
     * 付款用户的昵称
     */
    @TableField(exist = false)
    private String paymentUserNick;

    /**
     * 是否提醒 0,未提醒 1，已提醒
     */
    @TableField(value = "is_remind")
    private Integer isRemind;

    /**
     * 分享的比例
     */
    @TableField(value = "share_rate")
    private String shareRate;
    /**
     * 分享用户Id
     */
    @TableField(value = "share_user_id")
    private String shareUserId;


}
