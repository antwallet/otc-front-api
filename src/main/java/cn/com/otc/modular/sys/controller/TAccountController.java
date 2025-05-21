package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.redis.RedisOperate;
import cn.com.otc.common.utils.CheckTokenUtil;
import cn.com.otc.common.utils.HttpStatus;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.dict.service.SysDictDataService;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TAccount;
import cn.com.otc.modular.sys.bean.pojo.TAntwalletConfig;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.service.TAccountService;
import cn.com.otc.modular.sys.service.TAntwalletConfigService;
import cn.com.otc.modular.sys.service.TUserService;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/4
 */
@Slf4j
@RestController
@RequestMapping("/api/front/taccount")
public class TAccountController {

    @Autowired
    private TAccountService tAccountService;
    @Autowired
    private TUserService tUserService;
    @Autowired
    private SysDictDataService sysDictDataService;
    @Autowired
    private TAntwalletConfigService tAntwalletConfigService;
    @Autowired
    private CheckTokenUtil checkTokenUtil;

    private static final String ACCOUNT_TYPE = "account_type";//账户类型
    private static final String TRX = "TRX";
    private static final String USDT = "USDT";

    private static final String EXCHANGE_RATE_TRX = "EXCHANGE_RATE_TRX";//TRX汇率
    private static final String EXCHANGE_RATE_USDT = "EXCHANGE_RATE_USDT";//USDT汇率
    @Resource
    private RedisOperate redisOperate;
    /**
     * 根据token获取用户账户信息
     * @param exchangeRateType 1:USDT,2:php,3:inr
     * @return
     */
    @RequestMapping("/list")
    public R list(HttpServletRequest httpRequest,@RequestParam(defaultValue = StringUtils.EMPTY) String exchangeRateType) {
        try {

            /**
             * 1、根据token获取用户信息
             */
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);

            /**
             * 2、获取用户信息
             */
            LambdaQueryWrapper<TUser> lambdaQueryWrapper_user = new LambdaQueryWrapper<>();
            lambdaQueryWrapper_user.eq(TUser::getTgId, userInfoResult.getUserTGID());
            lambdaQueryWrapper_user.eq(TUser::getIslock, 0);
            TUser tUser = tUserService.getOne(lambdaQueryWrapper_user);
            if (tUser == null) {
                log.info("TAccountController.list 用户信息不存在,userInfoResult={}", JSONUtil.toJsonStr(userInfoResult));
                return R.error(ResultCodeEnum.USER_IS_NOT_EXIST.code, "用户不存在哦,请选择提供正确的用户哦!");
            }

            /**
             * 3、获取账户信息
             */
            LambdaQueryWrapper<TAccount> lambdaQueryWrapper_account = new LambdaQueryWrapper<>();
            lambdaQueryWrapper_account.eq(TAccount::getUserId, userInfoResult.getUserTGID());
            lambdaQueryWrapper_account.eq(TAccount::getAccountType, 1);
            lambdaQueryWrapper_account.eq(TAccount::getIslock, 0);
            List<TAccount> list = tAccountService.list(lambdaQueryWrapper_account);

            /**
             * 获取
             */
            TAntwalletConfig tAntwalletConfigTrx = tAntwalletConfigService.getById(EXCHANGE_RATE_TRX);
            TAntwalletConfig tAntwalletConfigUsdt = tAntwalletConfigService.getById(EXCHANGE_RATE_USDT);
            BigDecimal total_amount = new BigDecimal(BigDecimal.ZERO.toString());
            for (TAccount tAccount : list) {
                String accountTypeInfo = sysDictDataService.getDictLabel(ACCOUNT_TYPE, String.valueOf(tAccount.getAccountType()));
                tAccount.setAccountTypeInfo(accountTypeInfo);

                BigDecimal amount = new BigDecimal(tAccount.getAmount());
                tAccount.setAmount(amount.setScale(4, RoundingMode.DOWN).toString());
                if (accountTypeInfo.equals(TRX)) {
                    BigDecimal exchange_trx = new BigDecimal(tAntwalletConfigTrx.getPValue());
                    amount = amount.multiply(exchange_trx);
                } else if (accountTypeInfo.equals(USDT)) {
                    BigDecimal exchange_usdt = new BigDecimal(tAntwalletConfigUsdt.getPValue());
                    amount = amount.multiply(exchange_usdt);
                }
                tAccount.setExchangeMoney(amount.setScale(4, RoundingMode.DOWN).toString());
                total_amount = total_amount.add(amount);
            }
            /**
             * USDT换算汇率
             */
            BigDecimal exchangeRate = null;
            BigDecimal exchangeMoney = null;
            String exchangeType = "USD";
            try {
                if (StringUtils.isNotBlank(exchangeRateType)) {
                    if (exchangeRateType.equals("2")) {
                        String phpPrice = redisOperate.getRedis("PHP_PRICE");
                        exchangeRate = new BigDecimal(phpPrice);
                        exchangeMoney = total_amount.multiply(exchangeRate);
                        exchangeType = "PHP";
                    } else if (exchangeRateType.equals("3")) {
                        String inrPrice = redisOperate.getRedis("INR_PRICE");
                        exchangeRate = new BigDecimal(inrPrice);
                        exchangeMoney = total_amount.multiply(exchangeRate);
                        exchangeType = "INR";
                    }
                } else {
                    if ("菲律宾".equals(tUser.getCountry())) {
                        String phpPrice = redisOperate.getRedis("PHP_PRICE");
                        exchangeRate = new BigDecimal(phpPrice);
                        exchangeMoney = total_amount.multiply(exchangeRate);
                        exchangeType = "PHP";
                    } else if ("印度".equals(tUser.getCountry())) {
                        String inrPrice = redisOperate.getRedis("INR_PRICE");
                        exchangeRate = new BigDecimal(inrPrice);
                        exchangeMoney = total_amount.multiply(exchangeRate);
                        exchangeType = "INR";
                    }
                }

            } catch (Exception e) {
                log.error("tgId:{},获取汇率失败,具体失败信息:", userInfoResult.getUserTGID(), e);
            }

            return R.ok().put("list", list).put("total_amount", total_amount.setScale(4, RoundingMode.DOWN))
                    .put("exchangeType", exchangeType).put("exchangeMoney", exchangeMoney);
        } catch (Exception e) {
            log.error("根据token获取用户账户信息失败,具体失败信息:", e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "获取用户账户信息失败,请联系管理员!");
        }
    }


}
