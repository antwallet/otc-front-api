package cn.com.otc.modular.tron.dto.bean.result;

import lombok.Data;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/26
 */
@Data
public class ChargeQrcodeResult {
  private String orderId;

  private String userTGID;

  private String qrCodeImageBase64;

  private String base58CheckAddress;

  private String money;

  private Long expireTime;

  private Boolean isExistCharge;
}
