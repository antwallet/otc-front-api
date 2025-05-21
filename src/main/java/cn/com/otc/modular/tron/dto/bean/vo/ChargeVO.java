package cn.com.otc.modular.tron.dto.bean.vo;

import lombok.Data;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/26
 */
@Data
public class ChargeVO {
  /**
   * 充值类型 0,TRX 1,USDT
   */
  private String accountType;

  /**
   * 充值金额(最小金额1)
   */
  private String money;
}
