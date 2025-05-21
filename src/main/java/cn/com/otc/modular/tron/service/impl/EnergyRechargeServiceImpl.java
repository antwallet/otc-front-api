package cn.com.otc.modular.tron.service.impl;

import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.*;
import cn.com.otc.modular.dict.service.SysDictDataService;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TAccount;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.service.*;
import cn.com.otc.modular.tron.dto.bean.vo.UserBuyEnergyVO;
import cn.com.otc.modular.tron.service.EnergyRechargeService;
import cn.com.otc.modular.tron.util.MeFreeUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 能量充值 服务实现类
 * </p>
 *
 * @author 2024
 * @since 2024-07-09
 */
@Service
public class EnergyRechargeServiceImpl implements EnergyRechargeService {
    private static final Logger log = LoggerFactory.getLogger(EnergyRechargeServiceImpl.class);
    @Autowired
    private TAccountTradeService tAccountTradeService;
    @Autowired
    private TUserService tUserService;
    @Autowired
    private TAccountService tAccountService;
    @Autowired
    private SysDictDataService sysDictDataService;
    @Autowired
    private TAntwalletConfigService tAntwalletConfigService;
    @Autowired
    private TUserBuyEnergyService tUserBuyEnergyService;
    @Autowired
    private MeFreeUtil meFreeUtil;
    @Autowired
    private HutoolJWTUtil hutoolJWTUtil;
    @Autowired
    private HttpRequestUtil httpRequestUtil;
    @Autowired
    private MyCommonConfig myCommonConfig;
    @Autowired
    private AESUtils aesUtils;


    private static final String ACCOUNT_TYPE = "account_type";//账户类型
    private static final String TRX = "TRX";
    private static final String USDT = "USDT";

    private static final String TRADE_TYPE = "trade_type";
    private static final String TRADE_TYPE_BUY_PREMIUM = "购买能量";


    private static final int CHECK_ENERGY_STATUS_0 = 0;

    private static final String API_KEY = "7382805411";


    private static final String ENERGY_TYPE = "energy_type";//能量类型
    private static final String CONVERSION_PRICE = "CONVERSION_PRICE";//能量类型
    private static final String PRICE = "PRICE";//能量类型
    private static final String PREMIUM_PERCENTAGE = "PREMIUM_PERCENTAGE";//能量类型
    private static final String HOUR_PRICE_USDT = "HOUR_PRICE_USDT";//一笔一小时的USDT价格
    private static final String DAY_PRICE_USDT = "DAY_PRICE_USDT";//一笔一天的USDT价格
    private static final String FLASH_PRICE_USDT = "FLASH_PRICE_USDT";//速冲的USDT价格


    @Override
    public ResponseEntity<?> handleUserBuyEnergy(UserBuyEnergyVO userBuyEnergyVO, UserInfoResult userInfoResult, String lang) {
        log.info("开始进行购买能量 EnergyRechargeService.handleUserBuyEnergy userBuyEnergyVO={},userInfoResult={}",
                JSONUtil.toJsonStr(userBuyEnergyVO), JSONUtil.toJsonStr(userInfoResult));
        /**
         * 1、判断用户是否存在
         */
        TUser user = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
        if (null == user) {
            return ResponseEntity.failure(-1, "User does not exist!");
        }
        /**
         * 4、判断支付密码是否正确
         */
        /*String chargePsw = userBuyEnergyVO.getChargePassword();
        String decrypt = aesUtils.decrypt(chargePsw, "94E113C6A898CD39");
        if (!EncryptUtil.checkPswByBCrypt(decrypt, user.getChargePsw())) {
            return ResponseEntity.failure(ResultCodeEnum.USER_PSW_ERROR.code, I18nUtil.getMessage("6003", lang));
        }*/

        //单笔能量的价格
        BigDecimal energy_single_price = new BigDecimal(tAntwalletConfigService.selectTAntwalletbotConfigByPKey(HOUR_PRICE_USDT).getPValue());
        //单笔能量的价格*总笔数
        BigDecimal energy_price = energy_single_price.multiply(userBuyEnergyVO.getEnergyType());
        /**
         * 5、判断账户中是否有足够的金额为自己购买能量或者其他用户购买能量
         */
        Integer accountType = Integer.parseInt(sysDictDataService.getDictValue(ACCOUNT_TYPE, USDT));
        TAccount tAccount = tAccountService.getAccountByUserIdAndAccountType(userInfoResult.getUserTGID(), accountType);
        if (tAccount == null) {
            return ResponseEntity.failure(ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code, I18nUtil.getMessage("4004", lang));
        }
        BigDecimal amount = new BigDecimal(tAccount.getAmount());
        if (amount.compareTo(energy_price) < 0) {
            return ResponseEntity.failure(ResultCodeEnum.MONEY_NOT_ENOUGH.code, I18nUtil.getMessage("1020", lang));
        }

        /**
         * 6、调用meFreeUtil.order创建订单接口
         */
        Integer conversion_price = new Integer(tAntwalletConfigService.selectTAntwalletbotConfigByPKey(PRICE).getPValue());
        Integer quantity = userBuyEnergyVO.getEnergyType().multiply(BigDecimal.valueOf(conversion_price)).intValue();
        String order = MeFreeUtil.order(myCommonConfig.getEnergyApiKey(), myCommonConfig.getEnergyApiSecret(), 1, quantity
                , userBuyEnergyVO.getHexAddress(), 0);
        JSONObject jsonObject = new JSONObject(order);
        if (!jsonObject.get("code").equals(0)) {
            log.info("EnergyRechargeService.handleUserBuyEnergy 购买能量失败啦,order={}", order);
            return ResponseEntity.failure(ResultCodeEnum.ENERGY_BUY_ERROR.code, I18nUtil.getMessage("10004", lang));
        }

        /**
         * 7、购买成功记录数据
         */
        log.info("EnergyRechargeService.handleUserBuyEnergy 购买成功记录数据,userBuyEnergyVO={},userInfoResult={},jsonObject:{}", userBuyEnergyVO, userInfoResult, JSONUtil.toJsonStr(jsonObject));
        // 从根节点获取data对象
        JSONObject dataObject = jsonObject.getJSONObject("data");

        // 从data对象中获取pay_hash属性
        String payHash = dataObject.getStr("pay_hash");
        if (StringUtils.isBlank(payHash)) {
            payHash = "";
        }

        String energyBuyId = "energy_buy_".concat(UidGeneratorUtil.genId());//根据雪花算法获取提现id
        tUserBuyEnergyService.saveTUserBuyEnergy(energyBuyId, userInfoResult.getUserTGID(), energy_price.toString(),
                userBuyEnergyVO.getHexAddress(), quantity, 1, accountType, CHECK_ENERGY_STATUS_0, payHash);
        /**
         * 8、购买能量账户扣除对应的金额
         */
        BigDecimal amount_new = amount.subtract(energy_price);
        log.info("EnergyRechargeService.handleUserBuyEnergy 开始扣除购买能量账户对应的金额,付款账户id={},扣除金额={},账户金额={}", tAccount.getAccountId(), energy_price, amount);
        tAccountService.deductAccountMoney(tAccount.getId(), amount, energy_price);
        log.info("EnergyRechargeService.handleUserBuyEnergy 扣除购买能量账户余额成功,扣除金额={},账户金额={}", energy_price, amount_new);

        /**
         * 9、异步保存购买能量交易记录
         */
        Integer tradeType = Integer.parseInt(sysDictDataService.getDictValue(TRADE_TYPE, TRADE_TYPE_BUY_PREMIUM));
        String pay_trade_money = "-".concat(energy_price.toString());
        TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
        tAccountTradeService
                .saveAccountTrade(tAccount.getAccountId(), tUser.getUserId(), energyBuyId, pay_trade_money,
                        tradeType, amount_new.toString(), accountType, null);
        sendHandleUserBuyEnergyRobotMessage(userInfoResult.getUserTGID(), userInfoResult.getFirstName(), userInfoResult.getUserName(), lang);
        log.info("EnergyRechargeService.handleUserBuyEnergy 结束购买能量 userBuyEnergyVO={},userInfoResult={},energyBuyId={}",
                JSONUtil.toJsonStr(userBuyEnergyVO), JSONUtil.toJsonStr(tUser), energyBuyId);
        return ResponseEntity.success("购买成功");
    }

    @Override
    public String handlePurchasingPrice(String energyType, String rentTime, UserInfoResult userInfoResult, String lang) {

        if (StringUtils.isBlank(rentTime)) {
            log.warn("EnergyRechargeService.handlePurchasingPrice,rentTime={} 参数不能为空", rentTime);
            throw new RRException(I18nUtil.getMessage("1017", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
        }

        // 调用第三方接口 返回需要的数据
        Integer period = new Integer(rentTime);
        //从数据库中取一笔一小时时间的USDT价格
        BigDecimal hour_price_usdt = new BigDecimal(tAntwalletConfigService.selectTAntwalletbotConfigByPKey(HOUR_PRICE_USDT).getPValue());

        BigDecimal price = BigDecimal.ZERO;
        if (period == 1) { // 1小时
            price = hour_price_usdt.multiply(new BigDecimal(energyType));
            return price.toString();
        }
        return price.toString();
    }



    private String createSendMessageToken(String userTGID, String firstName, String userName) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("ip", NetUtil.getLocalhostStr());
        m.put("userTGID", userTGID);
        m.put("firstName", firstName);
        m.put("userName", userName);
        return hutoolJWTUtil.createTokenHut(m, myCommonConfig.getCommonTokenTimeOut(), myCommonConfig.getCommonTokenSecret());
    }

    /**
     * 功能描述: tg发送充值成功信息
     *
     * @auther: 2024
     * @date: 2024/7/10 下午3:36
     */
    private void sendHandleUserBuyEnergyRobotMessage(String tgId, String nick, String name, String lang) {
        String token = createSendMessageToken(tgId, nick, name);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("lang", lang);
        httpRequestUtil.doGetRequest(myCommonConfig.getTgHttpUrl().concat("/sendHandleUserBuyEnergyRobotMessage"),
                token, m, myCommonConfig.getTgHttpTimeOut());
    }

}
