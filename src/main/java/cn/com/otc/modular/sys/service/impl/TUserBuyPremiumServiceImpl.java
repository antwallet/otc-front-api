package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.redis.RedisOperate;
import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.*;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.bean.pojo.TUserBuyPremium;
import cn.com.otc.modular.sys.dao.TUserBuyPremiumDao;
import cn.com.otc.modular.sys.service.TAntwalletConfigService;
import cn.com.otc.modular.sys.service.TUserBuyPremiumService;
import cn.com.otc.modular.sys.service.TUserService;
import cn.com.otc.modular.tron.dto.bean.result.TPremiumType;
import cn.hutool.core.net.NetUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @description:用户购买会员表实现类
 * @author: zhangliyan
 * @time: 2024/7/14
 */
@Slf4j
@Service
public class TUserBuyPremiumServiceImpl extends ServiceImpl<TUserBuyPremiumDao, TUserBuyPremium> implements
        TUserBuyPremiumService {
    @Resource
    private RedisOperate redisOperate;
    @Autowired
    private CheckTokenUtil checkTokenUtil;
    @Autowired
    private TUserService tUserService;
    @Autowired
    private HutoolJWTUtil hutoolJWTUtil;
    @Autowired
    private HttpRequestUtil httpRequestUtil;
    @Autowired
    private MyCommonConfig myCommonConfig;
    @Autowired
    private TAntwalletConfigService tAntwalletConfigService;

    private static final int TIME_OUT = 8000;

    /**
     * 会员类型
     */
    private static final String PREMIUM_TYPE_3_MONTH_PRICE = "PREMIUM_TYPE_3_MONTH_PRICE";
    private static final String PREMIUM_TYPE_6_MONTH_PRICE = "PREMIUM_TYPE_6_MONTH_PRICE";
    private static final String PREMIUM_TYPE_1_YEAR_PRICE = "PREMIUM_TYPE_1_YEAR_PRICE";

    @Override
    public void saveTUserBuyPremium(String premiumBuyId, String payUserId, String buyUserId,
                                    String buyUserName, Integer premiumType, String money, Integer accountType) {

        TUserBuyPremium tUserBuyPremium = new TUserBuyPremium();
        tUserBuyPremium.setPremiumBuyId(premiumBuyId);
        tUserBuyPremium.setPayUserId(payUserId);
        tUserBuyPremium.setBuyUserId(buyUserId);
        tUserBuyPremium.setBuyUserName(buyUserName);
        tUserBuyPremium.setPremiumType(premiumType);
        tUserBuyPremium.setMoney(money);
        tUserBuyPremium.setAccountType(accountType);
        tUserBuyPremium.setCreateTime(LocalDateTime.now());
        this.save(tUserBuyPremium);
    }

    @Override
    public List<TUserBuyPremium> list(TUser tUser) {
        QueryWrapper<TUserBuyPremium> payUserId = new QueryWrapper<TUserBuyPremium>().eq("pay_user_id", tUser.getTgId());
        return this.list(payUserId);
    }

    @Override
    public ResponseEntity<?> getChat(String query, String lang) {
        String months = "3";  // 示例值，请根据实际需求修改
        String method = "searchPremiumGiftRecipient";  // 示例值，请根据实际需求修改
        String url = "https://fragment.com/api?hash=59cd208dc285d76f20";
        HashMap<String, Object> paramMap = new HashMap<>();

        paramMap.put("query", query);
        paramMap.put("months", months);
        paramMap.put("method", method);
        try {
            System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,SSLv3");
            HttpRequest request = HttpRequest.post(url)
                    .header("Cookie", "stel_ssid=1aa08641c5d4f4dfb4_14062368659014622100; stel_dt=-480; stel_token=d908d2d0a6647431bf71af337b96758ed908d2cbd908dd7019fb65fefefb39fc86bb0; stel_ton_token=upQ3QDK8jYE0OsqxpPL6faOFgVd1NUHglEqg0fF93TzW0zht_FJmxlauI9BDhJSDyGdN59zHsXJ9Wn7US6-lHpg2Btg9gDumYbADtJOXvvVvdoLfirx_vWhkKxldcqOM9ySpQq3miEhUwiubmf4Lvgq0uJA7S3uSxPM0SPnoGR1G6NJ_ZX2GzJAZh2F2abWEmAqB08UL")
                    .keepAlive(true)
                    .timeout(TIME_OUT);
            // 设置form-data
            request.form("query", query);
            request.form("months", months);
            request.form("method", method);

            HttpResponse httpResponse = request.execute();
            // 处理响应
            if (httpResponse.isOk()) {
                String body = httpResponse.body();
                Map<String, Object> tgResultMap = JSONUtil.toBean(body, Map.class);
                if (tgResultMap.get("error") != null) {
                    log.error("getChat-查询返回error：{},参数query：{}", tgResultMap.get("error").toString(), query);
                    //throw new RRException(tgResultMap.get("error").toString(), ResultCodeEnum.ILLEGAL_PARAMETER.code);
                    return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("606", lang));
                }
                log.warn("判断是否是真实用户：responseBody=" + JSONUtil.toJsonStr(body));
                log.warn("判断是否是真实用户：tgResultMap=" + JSONUtil.toJsonStr(tgResultMap));
                String name = tgResultMap.get("found").toString();
                Map<String, Object> map = JSONUtil.toBean(name, Map.class);
                log.warn("返回的结果：name={}" + name);
                //return map.get("name").toString();
                return ResponseEntity.success(map.get("name").toString());
            } else {
                //throw new RRException(I18nUtil.getMessage("606", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
                return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("606", lang));
            }
        } catch (HttpException e) {
            //throw new RRException(I18nUtil.getMessage("606", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
            return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("606", lang));
        }
    }

    @Override
    public R handlePremiumType(String lang, HttpServletRequest httpRequest, String exchangeRateType) {

        // 获取价格
        BigDecimal premium_type_3_month_price = new BigDecimal(tAntwalletConfigService.selectTAntwalletbotConfigByPKey(PREMIUM_TYPE_3_MONTH_PRICE).getPValue());
        BigDecimal premium_type_6_month_price = new BigDecimal(tAntwalletConfigService.selectTAntwalletbotConfigByPKey(PREMIUM_TYPE_6_MONTH_PRICE).getPValue());
        BigDecimal premium_type_1_year_price = new BigDecimal(tAntwalletConfigService.selectTAntwalletbotConfigByPKey(PREMIUM_TYPE_1_YEAR_PRICE).getPValue());

        String token = checkTokenUtil.getRequestToken(httpRequest);
        UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
        TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
        /**
         * USDT换算汇率
         */
        BigDecimal exchangeRate = null;
        String exchangeType = "USD";
        BigDecimal premium_type_3_month_price_exchangePrice = null;
        BigDecimal premium_type_6_month_price_exchangePrice = null;
        BigDecimal premium_type_1_year_price_exchangePrice = null;
        try {
            if (StringUtils.isNotBlank(exchangeRateType)) {
                if (exchangeRateType.equals("2")) {
                    String phpPrice = redisOperate.getRedis("PHP_PRICE");
                    exchangeRate = new BigDecimal(phpPrice);
                    premium_type_3_month_price_exchangePrice = premium_type_3_month_price.multiply(exchangeRate);
                    premium_type_6_month_price_exchangePrice = premium_type_6_month_price.multiply(exchangeRate);
                    premium_type_1_year_price_exchangePrice = premium_type_1_year_price.multiply(exchangeRate);
                    exchangeType = "PHP";
                } else if (exchangeRateType.equals("3")) {
                    String inrPrice = redisOperate.getRedis("INR_PRICE");
                    exchangeRate = new BigDecimal(inrPrice);
                    premium_type_3_month_price_exchangePrice = premium_type_3_month_price.multiply(exchangeRate);
                    premium_type_6_month_price_exchangePrice = premium_type_6_month_price.multiply(exchangeRate);
                    premium_type_1_year_price_exchangePrice = premium_type_1_year_price.multiply(exchangeRate);
                    exchangeType = "INR";
                }
            } else {
                if ("菲律宾".equals(tUser.getCountry())) {
                    String phpPrice = redisOperate.getRedis("PHP_PRICE");
                    exchangeRate = new BigDecimal(phpPrice);
                    premium_type_3_month_price_exchangePrice = premium_type_3_month_price.multiply(exchangeRate);
                    premium_type_6_month_price_exchangePrice = premium_type_6_month_price.multiply(exchangeRate);
                    premium_type_1_year_price_exchangePrice = premium_type_1_year_price.multiply(exchangeRate);
                    exchangeType = "PHP";
                } else if ("印度".equals(tUser.getCountry())) {
                    String inrPrice = redisOperate.getRedis("INR_PRICE");
                    exchangeRate = new BigDecimal(inrPrice);
                    premium_type_3_month_price_exchangePrice = premium_type_3_month_price.multiply(exchangeRate);
                    premium_type_6_month_price_exchangePrice = premium_type_6_month_price.multiply(exchangeRate);
                    premium_type_1_year_price_exchangePrice = premium_type_1_year_price.multiply(exchangeRate);
                    exchangeType = "INR";
                }
            }

        } catch (Exception e) {
            log.error("tgId:{},handlePremiumType获取汇率失败,具体失败信息:{}", userInfoResult.getUserId(), e.getMessage(), e);
        }

        List<TPremiumType> tPremiumTypes = new ArrayList<>();
        TPremiumType tPremiumType1 = new TPremiumType();
        tPremiumType1.setDuration("0");
        tPremiumType1.setPrice(premium_type_3_month_price.toString());
        if (null != premium_type_3_month_price_exchangePrice) {
            tPremiumType1.setExchangePrice(premium_type_3_month_price_exchangePrice.toString());
        }
        tPremiumTypes.add(tPremiumType1);
        TPremiumType tPremiumType2 = new TPremiumType();
        tPremiumType2.setDuration("1");
        tPremiumType2.setPrice(premium_type_6_month_price.toString());
        if (null != premium_type_6_month_price_exchangePrice) {
            tPremiumType2.setExchangePrice(premium_type_6_month_price_exchangePrice.toString());
        }
        tPremiumTypes.add(tPremiumType2);
        TPremiumType tPremiumType3 = new TPremiumType();
        tPremiumType3.setDuration("2");
        tPremiumType3.setPrice(premium_type_1_year_price.toString());
        if (null != premium_type_1_year_price_exchangePrice) {
            tPremiumType3.setExchangePrice(premium_type_1_year_price_exchangePrice.toString());
        }
        tPremiumTypes.add(tPremiumType3);

        tPremiumTypes.sort(Comparator.comparing(type -> Integer.parseInt(type.getDuration())));
        Map<String, Object> map = new HashMap<>();
        map.put("exchangeType", exchangeType);
        map.put("tPremiumTypes", tPremiumTypes);
        return R.ok(map);
    }


    private String createSendMessageToken(String userTGID, String firstName, String userName) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("ip", NetUtil.getLocalhostStr());
        m.put("userTGID", userTGID);
        m.put("firstName", firstName);
        m.put("userName", userName);
        return hutoolJWTUtil.createTokenHut(m, myCommonConfig.getCommonTokenTimeOut(), myCommonConfig.getCommonTokenSecret());
    }
}
