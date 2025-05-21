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
@TableName("t_payment_share_records")
public class TPaymentShareRecords implements Serializable {

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
     * 分享用户ID
     */
    @TableField(value = "share_user_id")
    private String shareUserId;
    /**
     * 邀请用户ID
     */
    @TableField(value = "invited_user_id")
    private String invitedUserId;
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


}
