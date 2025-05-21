package cn.com.otc.modular.sys.bean.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 账户表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_account")
public class TAccount implements Serializable {

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
     * 账户类型 0,TRX 1,USDT
     */
    @TableField("account_type")
    private Integer accountType;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 账户余额
     */
    @TableField("amount")
    private String amount;

    /**
     * 是否禁用 0,未禁用 1,禁用
     */
    @TableField("islock")
    private Short islock;

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

    @TableField(exist=false)
    private String accountTypeInfo;

    @TableField(exist=false)
    private String exchangeMoney;


}
