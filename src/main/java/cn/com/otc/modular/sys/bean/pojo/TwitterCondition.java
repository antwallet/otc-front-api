package cn.com.otc.modular.sys.bean.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("twitter_condition")
public class TwitterCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("twitter_nickname")
    private String twitterNickname;

    @TableField("tg_id")
    private String tgId;

    @TableField("status")
    private String status;

    @TableField("create_time")
    private String createTime;

    /**
     * 领取状态: 0-已领取 ,1-未领取
     */
    @TableField("receive_status")
    private int receiveStatus;
}
