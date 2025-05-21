package cn.com.otc.modular.tron.dto.bean.vo;

import lombok.Data;

/**
 * @description:用户购买会员
 * @author: zhangliyan
 * @time: 2024/7/14
 */
@Data
public class UserBuyPremiumVO {
   private Integer buyType;//购买类型,0给自己购买 1,给他们购买
   private String userId;//购买用户TGID
   private String userName;//购买用户TG名称
   private Integer premiumType;//会员类型
}
