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
 * 用户表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user_score")
public class TUserScore implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * TG的用户ID
     */
    @TableField("TG_id")
    private String tgId;

    /**
     * 分数类型
     */
    @TableField("score_type")
    private String scoreType;

    /**
     * 分数值
     */
    @TableField("score_value")
    private String scoreValue;
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
