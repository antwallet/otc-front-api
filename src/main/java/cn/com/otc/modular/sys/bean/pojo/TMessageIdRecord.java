package cn.com.otc.modular.sys.bean.pojo;

import lombok.Data;

import java.util.Date;

/**
 * @Auther: 2024
 * @Date: 2024/7/17 20:35
 * @Description: 点击按钮记录
 */
@Data
public class TMessageIdRecord {

    private Long id;
    /*信息id*/
    private String messageId;
    /*红包id*/
    private String redpacketId;

    private String shareUserTgId;
    /*创建时间*/
    private Date createTime;

}
