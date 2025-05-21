package cn.com.otc.modular.tron.dto.vo.result;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Auther: 2024
 * @Date: 2024/10/19 14:21
 * @Description: 收款前端bean
 */
@Data
public class TronTransResult {
    /**
     * 收款id
     */
    private String tronTransId;

    /**
     * 发送收款用户TGID
     */
    /*private String sendUserTgId;*/

    /**
     * 发送收款用户
     */
    private String sendUserName;

    /**
     * 发送收款昵称
     */
    private String sendUserNick;

    /**
     * 发送收款用户头像
     */
    private String sendUserAvatar;

    /**
     * 收款类型 0,人均模式 1,随机金额模式
     */
    private Integer tronTransType;

    /**
     * 收款个数
     */
    private Integer tronTransNum;

    /**
     * 收款总金额
     */
    private String money;

    /**
     * 用户付款金额
     */
    private Integer paymentAmount;
    /**
     * 付款用户昵称
     */
    private Integer paymentUserNick;
    /**
     * 付款用户用户名
     */
    private Integer paymentUserName;

    /**
     * 收款留言
     */
    private String comment;

    /**
     * 付款人数
     */
    private Integer paymentCount;

    /**
     * 收款状态 0,未收款 1,收款中 2,收款成功 3，收款失败 4、已结束 5、已过期
     */
    private Integer status;

    /**
     * 收款类型
     */
    private Integer accountType;

    /**
     * 领取条件----频道
     */
    private String channelConditions;
    /**
     * 领取条件----群组
     */
    private String groupsConditions;
    /**
     * 订阅过期时间
     */
    private LocalDateTime subscriptionExpiryTime;
    /**
     * 收款过期时间
     */
    private LocalDateTime paymentExpiryTime;
    /**
     * 订阅描述
     */
    private String subscriptionDesc;
    /**
     * 订阅时长
     */
    private Integer subscriptionHours;
    /**
     * 客服链接
     */
    private String customerServiceLink;
    /**
     * 客服链接
     */
    private String sharingRatio;
}
