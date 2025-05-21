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
 * 渠道表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-06-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_channelconfig")
public class TChannelConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 渠道id
     */
    @TableField("channel_id")
    private String channelId;

    /**
     * 渠道名称
     */
    @TableField("channel_name")
    private String channelName;

    /**
     * 渠道描述
     */
    @TableField("channel_desc")
    private String channelDesc;

    /**
     * 渠道秘钥
     */
    @TableField("channel_secret")
    private String channelSecret;

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



}
