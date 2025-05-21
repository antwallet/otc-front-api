package cn.com.otc.modular.sys.bean.result;

import lombok.Data;

/**
 * @description:返回的用户交易信息bean
 * @author: zhangliyan
 * @time: 2024/3/4
 */
@Data
public class TAccountTradeResult {
  private String money;
  private String exchangeMoney;
  private String accountTypeInfo;
  private String tradeTypeInfo;
  private String createTime;
  private String state;
  private String tradeNo;
}
