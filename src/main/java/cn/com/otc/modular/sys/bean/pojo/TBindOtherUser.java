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
 * 绑定账户表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-05-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_bind_other_user")
public class TBindOtherUser implements Serializable {

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
     * 绑定的用户TGID
     */
    @TableField("bind_tgid")
    private String bindTgid;

    /**
     * 绑定的用户名称
     */
    @TableField("bind_name")
    private String bindName;

    /**
     * 绑定的用户昵称
     */
    @TableField("bind_nick")
    private String bindNick;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(exist=false)
    private String bindUserInfo;

}
