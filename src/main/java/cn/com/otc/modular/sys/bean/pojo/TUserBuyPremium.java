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
 * 用户购买会员表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-07-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user_buy_premium")
public class TUserBuyPremium implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会员购买ID
     */
    @TableField("premium_buy_id")
    private String premiumBuyId;

    /**
     * 付款用户ID
     */
    @TableField("pay_user_id")
    private String payUserId;

    /**
     * 购买用户ID
     */
    @TableField("buy_user_id")
    private String buyUserId;

    /**
     * 购买用户名称
     */
    @TableField("buy_user_name")
    private String buyUserName;

    /**
     * 会员类型 0,3个月 1,6个月 2,1年
     */
    @TableField("premium_type")
    private Integer premiumType;

    /**
     * 会员价格
     */
    @TableField("money")
    private String money;

    /**
     * 会员价格类型
     */
    @TableField("account_type")
    private Integer accountType;

    /**
     * 购买状态 0,未审核 1,审核通过 2,审核失败
     */
    @TableField("status")
    private Integer status;

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

}
