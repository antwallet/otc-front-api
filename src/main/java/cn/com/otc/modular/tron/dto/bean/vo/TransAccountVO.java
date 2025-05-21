package cn.com.otc.modular.tron.dto.bean.vo;

import cn.com.otc.modular.tron.entity.bean.TTronTransConditionsBean;
import lombok.Data;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/26
 */
@Data
public class TransAccountVO {
  /**
   * 红包账户类型 0,TRX 1,USDT
   */
  private String accountType;

  /**
   * 转账金额
   */
  private String money;

  /**
   * 转账个数
   */
  private Integer transNum;

  /**
   * 订阅设置描述
   */
  private String subscriptionDesc;

  /**
   * 留言
   */
  private String comment;

  /**
   * 订阅过期时间
   */
  private Integer subscriptionTimeout;
  /**
   * 收款过期时间
   */
  private Integer paymentTimeout;
  /**
   * 收款类型(0,人均模式 1,随机金额模式)
   */
  private String transType;
  /**
   * 订阅条件---选择群组/频道
   */
  private TTronTransConditionsBean conditions;
  private String channelName;
  private String groupsName;
   /**
     * 客服链接
     */
    private String customerServiceLink;
    /**
     * 分享的比例
     */
    private String sharingRatio;
}
