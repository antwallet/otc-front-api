package cn.com.otc.modular.tron.entity.bean;

import lombok.Data;

/**
 * @description: 领取条件bean
 * @author: zhangliyan
 * @time: 2024/8/7
 */
@Data
public class TTronTransConditionsBean {

  /**
   * 选择的频道名称
   */
  private String channelName;
  /**
   * 选择的频道名称
   */
  private String channelBotName;

  /**
   * 选择的群组名称
   */
  private String groupName;
  /**
   * 选择的频道名称
   */
  private String groupBotName;

}
