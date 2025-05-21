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
 * 用户邀请关系表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-04-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_invited_user")
public class TInvitedUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 邀请的用户ID
     */
    @TableField("invited_user_id")
    private String invitedUserId;

    @TableField("activity_id")
    private String activityId;

    @TableField("redpacket_id")
    private String redpacketId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    /**
     * 是否发放邀请奖励。0：已发放，1：未发放
     */
    @TableField("hava_reward")
    private String havaReward;

}
