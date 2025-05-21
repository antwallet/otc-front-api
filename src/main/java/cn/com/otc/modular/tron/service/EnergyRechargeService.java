package cn.com.otc.modular.tron.service;

import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.tron.dto.bean.vo.UserBuyEnergyVO;

/**
 * <p>
 * 能量充值 服务类
 * </p>
 *
 * @author 2024
 * @since 2024-07-09
 */
public interface EnergyRechargeService  {

    /**
     *
     * 功能描述: 用户购买能量
     *
     * @auther: 2024
     * @date: 2024/7/30 14:26
     */
    ResponseEntity<?> handleUserBuyEnergy(UserBuyEnergyVO userBuyEnergyVO, UserInfoResult userInfoResult, String lang);

    /**
     *
     * 功能描述: 获取能量购买价格
     *
     * @auther: 2024
     * @date: 2024/7/30 14:26
     */
    String handlePurchasingPrice(String energyType, String rentTime, UserInfoResult userInfoResult, String lang);

}
