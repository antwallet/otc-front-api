package cn.com.otc.modular.tron.dto.bean;

import lombok.Data;

/**
 * @description:TRC20钱包信息
 * @author: zhangliyan
 * @time: 2024/2/28
 */
@Data
public class WalletBean {

  private String privateKey;//私钥

  private String publicKey;//公钥

  private String base58CheckAddress;// owner address in base58 钱包地址

  private String hexAddress;// owner address in hex 转账地址

}
