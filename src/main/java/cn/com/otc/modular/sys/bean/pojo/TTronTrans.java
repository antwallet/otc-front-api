package cn.com.otc.modular.sys.bean.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

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
@TableName("t_tron_trans")
public class TTronTrans implements Serializable {

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
     * 收款用户ID
     */
    @TableField(value = "send_user_id")
    private String sendUserId;

    /**
     * 收款币种 0,TRX 1,USDT
     */
    @TableField(value = "account_type")
    private Integer accountType;

    /**
     * 收款金额
     */
    @TableField(value = "money")
    private String money;

    /**
     * 收款类型 0,人均模式 1,随机金额模式
     */
    @TableField(value = "trans_type")
    private Integer transType;
    /**
     * 收款个数
     */
    @TableField(value = "trans_num")
    private Integer transNum;
    /**
     * 留言
     */
    @TableField(value = "comment")
    private String comment;

    /**
     * 收款状态 0,未收款 1,收款中 2,收款成功 3，收款失败
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 订阅描述
     */
    @TableField(value = "subscription_desc")
    private String subscriptionDesc;

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
     * 订阅过期时间
     */
    @TableField(value = "subscription_expiry_time")
    private LocalDateTime subscriptionExpiryTime;

    /**
     * 收款过期时间
     */
    @TableField(value = "payment_expiry_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentExpiryTime;

    /**
     * 订阅条件----群组
     */
    @TableField(value = "groups_conditions")
    private String groupsConditions;
    /**
     * 订阅条件-----频道
     */
    @TableField(value = "channel_conditions")
    private String channelConditions;
    /**
     * 付款人数
     */
    @TableField(value = "payment_count")
    private Integer paymentCount;
    /**
     * 订阅小时
     */
    @TableField(value = "subscription_hours")
    private Integer subscriptionHours;
    /**
     * 链接
     */
    @TableField(exist = false)
    private String link;
    /**
     * 客服链接
     */
    @TableField(value = "customer_service_link")
    private String customerServiceLink;
    /**
     * 分享的比例
     */
    @TableField(value = "sharing_ratio")
    private String sharingRatio;
}
