package cn.com.otc.modular.sys.bean.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Auther: 2024
 * @Date: 2024/7/10 14:11
 * @Description: 用户关注记录
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_follow_record")
public class TFollowRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /*领取用户tgid*/
    @TableField("user_id")
    private String userId;
    /*个人红包id*/
    @TableField("redpacket_id")
    private String redpacketId;
    /*状态 1：未关注2：已关注 3：关注已取消 4：再次关注*/
    @TableField("status")
    private Integer status;
    /*关注领取金额*/
    @TableField("money")
    private String money;
    /*领取时间*/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private Date createTime;
    /*修改时间*/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("update_time")
    private Date updateTime;

}
