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
 * 申述表
 * </p>
 *
 * @author zhangliyan
 * @since 2024-06-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_complaint")
public class TComplaint implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申述订单ID
     */
    @TableField("complaint_id")
    private String complaintId;

    /**
     * 付款订单ID
     */
    @TableField("payment_id")
    private String paymentId;

    /**
     * 申述用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 问题描述
     */
    @TableField("content")
    private String content;

    /**
     * 图片链接
     */
    @TableField("image_url")
    private String imageUrl;
    /**
     * 付款状态: 1-申述中, 2-申述成功, 3-申述失败
     */
    @TableField("status")
    private String status;

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
