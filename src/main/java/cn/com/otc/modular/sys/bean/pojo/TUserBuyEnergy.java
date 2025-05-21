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
 * 用户购买能量表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-07-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user_buy_energy")
public class TUserBuyEnergy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    /**
     * 购买能量ID
     */
    @TableField("energy_buy_id")
    private String energyBuyId;

    /**
     * 付款用户ID
     */
    @TableField("pay_user_id")
    private String payUserId;

    /**
     * 能量价格
     */
    @TableField("money")
    private String money;
    /**
     * 转账地址
     */
    @TableField("hex_address")
    private String hexAddress;

    /**
     * 转账笔数
     */
    @TableField("energy_type")
    private Integer energyType;

    /**
     * 支付价格类型 0,TRX 1,USDT
     */
    @TableField("account_type")
    private Integer accountType;

    /**
     * 购买状态 0,成功 1,失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 租用时间 单位小时
     */
    @TableField("rent_time")
    private Integer rentTime;

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
     * hash值
     */
    @TableField("pay_hash")
    private String payHash;

}
