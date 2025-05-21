package cn.com.otc.modular.tron.dto.bean.vo;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @description:用户购买能量
 * @author: zhangliyan
 * @time: 2024/7/14
 */
@Data
public class UserBuyEnergyVO {
   @Min(value = 1, message = "转账笔数最少1笔")
   @NotNull(message = "转账笔数不能为空")
   private BigDecimal energyType;//转账笔数
   //private Integer rentTime;//租用时间
   //private String money;//支付价格
   @NotBlank(message = "支付地址不能为空")
   private String hexAddress;//支付地址
  /* @NotBlank(message = "支付密码不能为空")
   private String chargePassword;//支付密码*/
}
