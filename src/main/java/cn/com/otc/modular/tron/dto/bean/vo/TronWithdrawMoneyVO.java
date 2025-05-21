package cn.com.otc.modular.tron.dto.bean.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/28
 */
@Data
public class TronWithdrawMoneyVO {
   private String hexAddress;
   private String accountType;
   /**
    * 提现类型 2:印度 3:菲律宾
    */
   @NotBlank(message = "blockchainType can bot be null")
   @Pattern(regexp = "^[0-3]$", message = "blockchainType must be between 0 and 3")
   private String blockchainType;
    /**
     * 1-gcash,2-maya
     */
    private String philippinesWithdrawalType;
   /**
    * 提现金额
    */
   @NotBlank(message = "money can bot be null")
   private String money;
   /*支付密码*/
   //private String chargePsw;

    /**
     * 银行名称
     */
    private String bankName;
    /**
     * 收款人姓名
     * 收款人姓名請填 「Gcash收款手機號」若是代付至銀行 請填入正確姓名
     */
    private String bankCardName;
    /**
     * 收款人账号
     * 收款人账号請填「Gcash收款手機號」若是代付至銀行 請填入正確帳號
     */
    private String bankCardAccount;

    private String IFSC;
}
