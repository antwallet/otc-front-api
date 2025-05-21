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
 * 重置密码记录表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_password_reset_log")
public class TPasswordResetLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * TG的用户ID
     */
    @TableField("tg_id")
    private String tgId;
    /**
     * TG的用户ID
     */
    @TableField("pass_word")
    private String passWord;

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
