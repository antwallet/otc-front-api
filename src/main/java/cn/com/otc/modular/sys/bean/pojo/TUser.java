package cn.com.otc.modular.sys.bean.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
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
@TableName("t_user")
public class TUser implements Serializable {

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
     * TG的用户ID
     */
    @TableField("TG_id")
    private String tgId;

    /**
     * 用户名称
     */
    @TableField("`name`")
    private String name;

    /**
     * 用户昵称
     */
    @TableField("nick")
    private String nick;

    /**
     * 用户性别
     */
    @TableField("sex")
    private String sex;

    /**
     * 用户手机
     */
    @TableField("phone")
    private String phone;

    /**
     * 用户邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 是否禁用 0,未禁用 1,禁用
     */
    @TableField("islock")
    private Short islock;

    /**
     * 登录账号
     */
    @TableField("login_name")
    private String loginName;

    /**
     * 登陆密码
     */
    @TableField("`password`")
    private String password;

    /**
     * 登录时间
     */
    @TableField("login_time")
    private LocalDateTime loginTime;

    /**
     * 登录渠道 TG登录或者普通登录
     */
    @TableField("login_channel")
    private String loginChannel;

    /**
     * 支付密码
     */
    @TableField("charge_psw")
    private String chargePsw;

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

    /**
     * 红包封面
     */
    @TableField("redpacket_cover_img")
    private String redpacketCoverImg;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 是否是会员 -- true：是。 false：否
     */
    @TableField("is_premium")
    private Boolean isPremium;
    /**
     * ip地址
     */
    @TableField("ip_address")
    private String ipAddress;
    /**
     * 设备信息
     */
    @TableField("device_model")
    private String deviceModel;
    /**
     * 注册日期
     */
    @TableField("registration_date")
    private String registrationDate;

    /**
     * 是否展示弹窗 0,弹出 1,不弹
     */
    @TableField("isshow_panel")
    private Short isshowPanel;

    @TableField("user_score")
    private BigDecimal userScore;
    /**
     * 人机验证。0：验证成功 1：验证失败
     */
    @TableField("captcha_verified")
    private String captchaVerified;

    @TableField("user_img_verified")
    private String userImgVerified;
    /**
     * 国家
     */
    @TableField("country")
    private String country;
    /**
     * 语言
     */
    @TableField("language")
    private String language;
    /**
     * 分辨率
     */
    @TableField("resolution")
    private String resolution;
    /**
     * 网络类型
     */
    @TableField("network_type")
    private String networkType;
    /**
     * 网络状态
     */
    @TableField("network_status")
    private String networkStatus;
}
