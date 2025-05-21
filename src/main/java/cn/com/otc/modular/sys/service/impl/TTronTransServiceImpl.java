package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.common.constants.CommonConstant;
import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.redis.RedisOperate;
import cn.com.otc.common.utils.*;
import cn.com.otc.modular.dict.service.SysDictDataService;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.*;
import cn.com.otc.modular.sys.bean.vo.TronTransVo;
import cn.com.otc.modular.sys.dao.TTronTransDao;
import cn.com.otc.modular.sys.service.*;
import cn.hutool.core.net.NetUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/26
 */
@Service
public class TTronTransServiceImpl extends ServiceImpl<TTronTransDao, TTronTrans> implements
        TTronTransService {
    private static final Logger log = LoggerFactory.getLogger(TTronTransService.class);

    private static final String BASE62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_LINK_LENGTH = 20; // 控制短链接的长度
    @Resource
    private CommonUtil commonUtil;
    @Autowired
    private RedisOperate redisOperate;
    @Resource
    private SharedCache userMapLocalCache;
    @Autowired
    private TPaymentRecordsService tPaymentRecordsService;
    @Autowired
    private SysDictDataService sysDictDataService;
    @Autowired
    private TUserService tUserService;
    @Autowired
    private TAccountService tAccountService;
    @Autowired
    private TAccountTradeService tAccountTradeService;
    @Autowired
    private TAntwalletConfigService tAntwalletConfigService;
    @Autowired
    private HutoolJWTUtil hutoolJWTUtil;
    @Autowired
    private HttpRequestUtil httpRequestUtil;
    @Autowired
    private MyCommonConfig myCommonConfig;
    @Autowired
    private TPaymentShareRecordsService tPaymentShareRecordsService;

    /**
     * 收款状态 0,未收款 1,收款中 2,收款成功 3，收款失败
     */
    private static final String TRON_TRANS_STATUS = "tron_trans_status";
    private static final String TRON_TRANS_STATUS_0 = "未收款";
    private static final String TRON_TRANS_STATUS_1 = "收款中";
    private static final String TRON_TRANS_STATUS_2 = "收款成功";
    private static final String TRON_TRANS_STATUS_3 = "收款失败";
    private static final String TRON_TRANS_STATUS_4 = "已结束";
    private static final String TRON_TRANS_STATUS_5 = "已过期";


    /**
     * 付款状态 0,已付款 1,申述中 2,申述成功 3,申述失败 4,付款成功
     */
    private static final String PAYMENT_STATUS = "payment_status";
    private static final String PAYMENT_STATUS_0 = "已付款";
    private static final String PAYMENT_STATUS_1 = "申述中";
    private static final String PAYMENT_STATUS_2 = "申述成功";
    private static final String PAYMENT_STATUS_3 = "申述失败";
    private static final String PAYMENT_STATUS_4 = "付款成功";

    private static final String RECEIVING_FEE = "RECEIVING_FEE";

    /**
     * 交易类型
     */
    private static final String TRADE_TYPE = "trade_type";
    private static final String TRADE_TYPE_COLLECTION = "收款";

    @Override
    public TTronTrans getTTronTrans(String tronTransId) {
        LambdaQueryWrapper<TTronTrans> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(TTronTrans::getTronTransId, tronTransId);
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    public void saveTTronTrans(String tronTransId, String sendUserId, Integer accountType, String money, Integer transNum,
                               Integer transType, String groupsConditions, String channelConditions, String comment,
                               Integer paymentExpiryTime, Integer subscriptionExpiryTime, String subscriptionDesc,
                               String customerServiceLink,String sharingRatio) {
        TTronTrans tTronTrans = new TTronTrans();
        tTronTrans.setTronTransId(tronTransId);
        tTronTrans.setSendUserId(sendUserId);
        tTronTrans.setTransNum(transNum);
        tTronTrans.setTransType(transType);
        if (StringUtil.isNotEmpty(groupsConditions)) {
            tTronTrans.setGroupsConditions(groupsConditions);
        }
        if (StringUtil.isNotEmpty(channelConditions)) {
            tTronTrans.setChannelConditions(channelConditions);
        }
        tTronTrans.setAccountType(accountType);
        tTronTrans.setComment(comment);
        tTronTrans.setMoney(money);
        tTronTrans.setStatus(0);
        long now = System.currentTimeMillis();
        tTronTrans.setCreateTime(TimeUtil.getDateTimeOfTimestamp(now));
        if (paymentExpiryTime != 0) {
            tTronTrans.setPaymentExpiryTime(TimeUtil.getDateTimeOfTimestamp(TimeUtil.getAfterDate(now, subscriptionExpiryTime, ChronoUnit.HOURS)));
            //tTronTrans.setPaymentExpiryTime(TimeUtil.getDateTimeOfTimestamp(TimeUtil.getAfterDate(now, 10, ChronoUnit.MINUTES)));
        }
        if (subscriptionExpiryTime != 0) {
            tTronTrans.setSubscriptionExpiryTime(TimeUtil.getDateTimeOfTimestamp(TimeUtil.getAfterDate(now, subscriptionExpiryTime, ChronoUnit.HOURS)));
            tTronTrans.setSubscriptionHours(subscriptionExpiryTime);
        }
        tTronTrans.setSubscriptionDesc(subscriptionDesc);
        tTronTrans.setCustomerServiceLink(customerServiceLink);
        if (StringUtil.isNotEmpty(sharingRatio)){
            tTronTrans.setSharingRatio(sharingRatio);
        }

        this.save(tTronTrans);
    }

    @Override
    @Async
    public void modifyTTronTransStatus(Long id, Integer status) {
        TTronTrans tTronTrans = new TTronTrans();
        tTronTrans.setId(id);
        tTronTrans.setStatus(status);
        tTronTrans.setUpdateTime(LocalDateTime.now());
        this.updateById(tTronTrans);
    }

    @Override
    public void addTronTransPaymentCount(TTronTrans tTronTrans, int addPaymentCount) {
        tTronTrans.setPaymentCount(tTronTrans.getPaymentCount() + addPaymentCount);
        redisOperate.setRedis(CommonConstant.SEND_GROUP_REDPACKET + tTronTrans.getTronTransId(), tTronTrans);

        tTronTrans.setUpdateTime(LocalDateTime.now());
        this.updateById(tTronTrans);
    }

    /*@Override
    public void handlePaymentRecordsExpire() {
        log.debug("开始处理过期未申述付款");
        *//**
         * 1、获取未申述已过期的用户列表
         *//*
        List<TPaymentRecords> tPaymentRecordsList = tPaymentRecordsService.getTPaymentRecordsList();
        if (tPaymentRecordsList.isEmpty()) {
            return;
        }

        *//**
         * 2、将过期的付款记录修改状态并且把扣除的金额返回到收款用户中
         *//*
        Integer payment_status_4 = Integer.parseInt(sysDictDataService.getDictValue(PAYMENT_STATUS, PAYMENT_STATUS_4));
        for (TPaymentRecords tPaymentRecords : tPaymentRecordsList) {
            //将金额退回到原账号
            try {
                backTPaymentRecordsMoneyToAccount(tPaymentRecords);
                tPaymentRecordsService.modifyTPaymentRecordsStatus(tPaymentRecords.getId(), payment_status_4);
            } catch (Exception e) {
                log.error(String.format("将金额转给账户失败,tSendSingleRedpacket=%s,具体失败信息:", JSONUtil.toJsonStr(tPaymentRecords)), e);
                continue;
            }
        }
        log.debug("结束处理过期未申述付款");
    }*/

    @Override
    public List<TTronTrans> getTTronTransByGroupsName(String chatUserName) {
        LambdaQueryWrapper<TTronTrans> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(TTronTrans::getGroupsConditions, chatUserName);
        return this.list(lambdaQueryWrapper);
    }
    @Override
    public List<TTronTrans> getTTronTransByChannelName(String chatUserName) {
        LambdaQueryWrapper<TTronTrans> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(TTronTrans::getChannelConditions, chatUserName);
        return this.list(lambdaQueryWrapper);
    }

    @Override
    public List<TTronTrans> tTronTransList(UserInfoResult userInfoResult, String lang) {
        LambdaQueryWrapper<TTronTrans> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(TTronTrans::getSendUserId, userInfoResult.getUserTGID());
        List<TTronTrans> list = this.list(lambdaQueryWrapper);
        if (list.isEmpty()){
            return Collections.emptyList();
        }
        TUser tUser = userMapLocalCache.get(userInfoResult.getUserTGID());
        if (null == tUser) {
            tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
            if (tUser == null) {
                log.error("TTronTransService.tTronTransList 收款用户不存在,shareUserId={}",
                         userInfoResult.getUserTGID());
                throw new RRException(I18nUtil.getMessage(ResultCodeEnum.USER_IS_NOT_EXIST.code.toString(), lang), ResultCodeEnum.USER_IS_NOT_EXIST.code);
            }
        }
        for (TTronTrans tTronTrans : list) {
            try {
                String tronTransId = tTronTrans.getTronTransId().concat(":").concat(tUser.getUserId());
                tTronTrans.setLink("");
                tTronTrans.setTronTransId(tronTransId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return list;
    }

    // 生成短链接
    /*public String generateShortLink(String originalData) throws NoSuchAlgorithmException {
        //1.生成短链前先去数据库查询，有数据代表之前就生成过，就直接返回
        ActivityShortLinkEntity linkEntity = activityShortLinkDao.queryByOriginalData(originalData);
        if (null != linkEntity) {
            log.info("originalData:{},查询到该链接，直接返回", originalData);
            return linkEntity.getShortLink();
        }
        //2.生成短链接
        // 使用SHA-256生成哈希值
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(originalData.getBytes(StandardCharsets.UTF_8));
        // 使用Base62编码将哈希值转换为短字符串
        String base62Encoded = base62Encode(hashBytes);
        // 截取前20位，控制长度
        String shortLink = base62Encoded.substring(0, SHORT_LINK_LENGTH);
        // 保存原始数据和短链接的映射
        ActivityShortLinkEntity entity = new ActivityShortLinkEntity();
        entity.setShortLink(shortLink);
        entity.setOriginalData(originalData);
        activityShortLinkDao.insert(entity);
        return shortLink;
    }*/

    // Base62编码
    private String base62Encode(byte[] input) {
        BigInteger bigInt = new BigInteger(1, input);  // 将字节数组转换为一个大整数
        StringBuilder result = new StringBuilder();
        while (bigInt.compareTo(BigInteger.ZERO) > 0) {
            int remainder = bigInt.mod(BigInteger.valueOf(62)).intValue();
            result.append(BASE62.charAt(remainder));
            bigInt = bigInt.divide(BigInteger.valueOf(62));
        }
        return result.reverse().toString();  // 反转字符串，因为我们从最低位开始编码
    }

    @Override
    public void handleStopPayment(String id, String lang) {
        if (StringUtil.isBlank(id)){
            log.warn("TTronTransService.handleStopPayment 非法参数,具体信息 id:{}", id);
            throw new RRException(I18nUtil.getMessage("1017", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
        }
        Integer tron_trans_status_4 = Integer.parseInt(sysDictDataService.getDictValue(TRON_TRANS_STATUS, TRON_TRANS_STATUS_4));
        TTronTrans tTronTrans = new TTronTrans();
        tTronTrans.setId(Long.valueOf(id));
        tTronTrans.setStatus(tron_trans_status_4);
        tTronTrans.setUpdateTime(LocalDateTime.now());
        tTronTrans.setPaymentExpiryTime(LocalDateTime.now());
        this.updateById(tTronTrans);
    }

    public List<TTronTrans> getTTronTrans() {
        LambdaQueryWrapper<TTronTrans> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TTronTrans::getStatus, 0).or().eq(TTronTrans::getStatus, 1);
        List<TTronTrans> tPaymentRecordsList = this.list(lambdaQueryWrapper);
        return tPaymentRecordsList.stream()
                .filter(tTronTrans -> tTronTrans.getSubscriptionHours() > 48)
                .collect(Collectors.toList());
    }

    public List<TTronTrans> handlePaymentExpirationStatus() {
        LambdaQueryWrapper<TTronTrans> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TTronTrans::getStatus, 0).or().eq(TTronTrans::getStatus, 1);
        List<TTronTrans> tPaymentRecordsList = this.list(lambdaQueryWrapper);
        long now = System.currentTimeMillis() / 1000;
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        return tPaymentRecordsList.stream()
                .filter(tTronTrans -> tTronTrans.getPaymentExpiryTime()!=null && tTronTrans.getPaymentExpiryTime().atZone(zoneId).toEpochSecond() <= now)
                .collect(Collectors.toList());
    }

    /**
     * 将未申述用户金额发送给收款用户
     *
     * @param tPaymentRecords
     */
    private void backTPaymentRecordsMoneyToAccount(TPaymentRecords tPaymentRecords) {

        /**
         * 1、判断发送红包的用户是否存在
         */
        //用户信息从缓存获取，
        // 没有查询数据库，放入本地缓存
        TUser sendTUser = userMapLocalCache.get(tPaymentRecords.getSendUserId());
        if (null == sendTUser) {
            sendTUser = tUserService.getTUserByTGId(tPaymentRecords.getSendUserId());
            if (sendTUser == null) {
                log.error("TTronTransService.backTPaymentRecordsMoneyToAccount 发送红包的用户不存在,tronTransId={},sendUserId={}",
                        tPaymentRecords.getTronTransId(), tPaymentRecords.getSendUserId());
                throw new RRException("发送红包的用户不存在", ResultCodeEnum.USER_IS_NOT_EXIST.code);
            }
        }
        /**
         * 2、获取领取的金额和账户金额
         */
        BigDecimal money;
        BigDecimal shareMoney;
        try {
            money = new BigDecimal(tPaymentRecords.getPaymentMoney());
            shareMoney = BigDecimal.ZERO;
        } catch (Exception e) {
            log.error(String.format("TTronTransService.backTPaymentRecordsMoneyToAccount 红包金额的格式有问题,发送红包用户[%s],账户类型[%s]," + "具体付款信息:%s", tPaymentRecords.getSendUserId(), tPaymentRecords.getAccountType(), JSONUtil.toJsonStr(tPaymentRecords)), e);
            throw new RRException("金额的格式有问题哦,应该是小数点后面两位!", ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
        }

        /**
         * 3、获取收款的订单信息
         */
        if (StringUtils.isNotBlank(tPaymentRecords.getShareRate()) && !tPaymentRecords.getPaymentUserId().equals(tPaymentRecords.getShareUserId())){
            log.info("RedPacketManageService.backTPaymentRecordsMoneyToAccount 开始处理分享的比例,比例值是:{}", tPaymentRecords.getShareRate());
            //2、获取分享人应该得到的金额
            shareMoney=money.multiply(new BigDecimal(tPaymentRecords.getShareRate()));
            //异步统计分享的人数
            tPaymentShareRecordsService.saveTPaymentShareRecords(tPaymentRecords.getTronTransId(), tPaymentRecords.getPaymentId(), tPaymentRecords.getPaymentUserId(), tPaymentRecords.getShareUserId());
        }
        /**
         * 4、获取发送个人红包的账户,并判断是否不存在账户
         */
        TAccount tAccount = tAccountService.getAccountByUserIdAndAccountType(sendTUser.getTgId(), tPaymentRecords.getAccountType());
        if (tAccount == null) {
            log.warn("TTronTransService.backTPaymentRecordsMoneyToAccount 没有查到收款的账户信息,tronTransId={},发送红包用户[{}],账户类型[{}]," + "具体付款信息:{}",
                    tPaymentRecords.getTronTransId(), tPaymentRecords.getSendUserId(), tPaymentRecords.getAccountType(), JSONUtil.toJsonStr(tPaymentRecords));
            throw new RRException("没有查到发送红包的账户信息,请确认是否有对应的账户!", ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code);
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(tAccount.getAmount());
        } catch (Exception e) {
            log.error(String.format("TTronTransService.backTPaymentRecordsMoneyToAccount 账户金额的格式有问题,发送红包用户[%s],账户类型[%s]," + "具体付款信息:%s",
                    tPaymentRecords.getSendUserId(), tPaymentRecords.getAccountType(), JSONUtil.toJsonStr(tPaymentRecords)), e);
            throw new RRException("账户金额的格式有问题,应该是小数点后面两位!", ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
        }

        //扣除每笔的手续费
        String pValue = tAntwalletConfigService.getById(RECEIVING_FEE).getPValue();


        money = money.subtract(money.multiply(new BigDecimal(pValue))).subtract(shareMoney).setScale(4, RoundingMode.DOWN);
        log.info("TTronTransService.backTPaymentRecordsMoneyToAccount 开始扣除手续费,手续费为:{},分享抽成:{},实际金额:{}", pValue, shareMoney,money);
        /**
         * 4、金额返还到账户中
         */
        log.info("TTronTransService.backTPaymentRecordsMoneyToAccount 开始将金额发放给到账户,账户ID={},增加金额={},账户金额={}", tAccount.getAccountId(), money, amount);
        tAccountService.addAccountMoney(tAccount.getId(), amount, money);
        log.info("TTronTransService.backTPaymentRecordsMoneyToAccount 金额返还到账户成功,账户ID={},增加金额={},账户金额={}", tAccount.getAccountId(), money, amount.add(money));

        /*
         * 5、异步保存交易记录
         */
        Integer tradeType = Integer.parseInt(sysDictDataService.getDictValue(TRADE_TYPE, TRADE_TYPE_COLLECTION));
        String trade_money = "+".concat(money.toString());
        BigDecimal amount_new = amount.add(money);
        tAccountTradeService.saveAccountTrade(tAccount.getAccountId(), sendTUser.getUserId(), tPaymentRecords.getTronTransId(),
                trade_money, tradeType, amount_new.toString(), tPaymentRecords.getAccountType(), null);
        //发送付款通知
        HashMap<String, Object> map = new HashMap<>();
        map.put("time",LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        map.put("money",money);
        map.put("lang",sendTUser.getLanguage());
        sendTGMessage(sendTUser.getTgId(), sendTUser.getNick(), sendTUser.getName(), "handleUserPaymentMessage",  map);

    }



    private void sendTGMessage(String tgId, String name, String nick, String httpName, Map<String, Object> paramMap) {
        CompletableFuture.runAsync(() -> {
            log.info("TTronTransService.sendTGMessage 发送TG消息,tgId={},name={},nick={},httpName={},paramMap={}", tgId, name, nick, httpName, JSONUtil.toJsonStr(paramMap));
            String token = createSendMessageToken(tgId, nick, name);
            httpRequestUtil.doGetRequest(myCommonConfig.getTgHttpUrl().concat(httpName), token, paramMap, myCommonConfig.getTgHttpTimeOut());
        });
    }



    /**
     * 红包系统中生成token,有效期2小时
     *
     * @param userTGID
     * @param firstName
     * @param userName
     * @return
     */
    private String createSendMessageToken(String userTGID, String firstName, String userName) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("ip", NetUtil.getLocalhostStr());
        m.put("userTGID", userTGID);
        m.put("firstName", firstName);
        m.put("userName", userName);
        return hutoolJWTUtil.createTokenHut(m, myCommonConfig.getCommonTokenTimeOut(), myCommonConfig.getCommonTokenSecret());
    }
}
