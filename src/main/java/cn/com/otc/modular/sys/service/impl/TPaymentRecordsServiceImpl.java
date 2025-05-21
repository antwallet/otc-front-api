package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.common.utils.*;
import cn.com.otc.modular.dict.service.SysDictDataService;
import cn.com.otc.modular.sys.bean.result.TPaymentRecordsResult;
import cn.com.otc.modular.sys.bean.pojo.TPaymentRecords;
import cn.com.otc.modular.sys.bean.vo.TPaymentRecordsVo;
import cn.com.otc.modular.sys.dao.TPaymentRecordsDao;
import cn.com.otc.modular.sys.service.*;
import cn.hutool.core.net.NetUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/26
 */
@Service
public class TPaymentRecordsServiceImpl extends ServiceImpl<TPaymentRecordsDao, TPaymentRecords> implements
        TPaymentRecordsService {
    private static final Logger log = LoggerFactory.getLogger(TPaymentRecordsService.class);


    /**
     * 付款状态 0,已付款 1,申述中 2,申述成功 3,申述失败 4,付款成功 5、订阅已过期
     */
    private static final String PAYMENT_STATUS = "payment_status";
    private static final String PAYMENT_STATUS_0 = "已付款";
    private static final String PAYMENT_STATUS_1 = "申述中";
    private static final String PAYMENT_STATUS_2 = "申述成功";
    private static final String PAYMENT_STATUS_3 = "申述失败";
    private static final String PAYMENT_STATUS_4 = "付款成功";
    private static final String PAYMENT_STATUS_5 = "订阅已过期";


    private static final long FORTY_EIGHT_HOURS_IN_SECONDS = 48 * 60 * 60; // 48小时转换为秒
    //private static final long FORTY_EIGHT_HOURS_IN_SECONDS = 120; // 48小时转换为秒

    @Autowired
    private HutoolJWTUtil hutoolJWTUtil;
    @Autowired
    private HttpRequestUtil httpRequestUtil;
    @Autowired
    private MyCommonConfig myCommonConfig;
    @Autowired
    private SysDictDataService sysDictDataService;

    @Autowired
    private TUserService tUserService;
    @Override
    public TPaymentRecords getLatestPaymentRecord(String tronTransId, String paymentUserId) {
        LambdaQueryWrapper<TPaymentRecords> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(TPaymentRecords::getTronTransId, tronTransId);
        lambdaQueryWrapper.eq(TPaymentRecords::getPaymentUserId, paymentUserId);
        lambdaQueryWrapper.orderByDesc(TPaymentRecords::getCreateTime);
        lambdaQueryWrapper.last("LIMIT 1");
        return this.getOne(lambdaQueryWrapper);
    }


    @Override
    public void saveTPaymentRecords(String tronTransId, String paymentId, String sendUserId, String userId, String paymentMoney,
                                    Integer accountType, long durationInHours, String chatName, Integer status, String sharingRatio, String shareUserId) {
        TPaymentRecords tPaymentRecords = new TPaymentRecords();
        //long now = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();
        tPaymentRecords.setTronTransId(tronTransId);
        tPaymentRecords.setPaymentId(paymentId);
        tPaymentRecords.setSendUserId(sendUserId);
        tPaymentRecords.setPaymentUserId(userId);
        tPaymentRecords.setAccountType(accountType);
        tPaymentRecords.setPaymentMoney(paymentMoney);
        if (StringUtils.isNotBlank(chatName)){
            tPaymentRecords.setChatBotName(chatName);
        }
        tPaymentRecords.setStatus(status);
        tPaymentRecords.setCreateTime(now);
        tPaymentRecords.setPaymentTime(now);
        /*tTronTrans.setExpiryTime(TimeUtil.getDateTimeOfTimestamp(TimeUtil.getAfterDate(now, 24,
                ChronoUnit.HOURS)));*/
        tPaymentRecords.setExpiryTime(now.plusSeconds(60));
        if (durationInHours != 0){
            tPaymentRecords.setSubscriptionExpiryTime(now.plusMinutes(durationInHours));
            /*tPaymentRecords.setSubscriptionExpiryTime(TimeUtil.getDateTimeOfTimestamp(TimeUtil.getAfterDate(System.currentTimeMillis(), 10, ChronoUnit.MINUTES)))*/;
        }
        if (StringUtils.isNotBlank(sharingRatio)) {
            tPaymentRecords.setShareRate(sharingRatio);
        }
        tPaymentRecords.setShareUserId(shareUserId);
        this.save(tPaymentRecords);
    }

    @Override
    public void modifyTPaymentRecordsStatus(Long id, Integer status) {
        TPaymentRecords paymentRecords = new TPaymentRecords();
        paymentRecords.setId(id);
        paymentRecords.setStatus(status);
        paymentRecords.setUpdateTime(LocalDateTime.now());
        this.updateById(paymentRecords);

    }

    public void updatePaymentRecordToExpired(Long id, Integer newStatus) {
        TPaymentRecords record = this.getById(id);
        if (record != null && record.getStatus() == 4) { // 假设 4 是未过期的状态
            record.setStatus(newStatus);
            this.updateById(record);
        }
    }

    @Override
    public TPaymentRecordsResult tPaymentRecordsList(TPaymentRecordsVo tPaymentRecordsVo, String lang) {
        tPaymentRecordsVo.setPageIndex((tPaymentRecordsVo.getPageIndex() - 1) * tPaymentRecordsVo.getPageSize());
        Integer count = this.baseMapper.queryByPageCount(tPaymentRecordsVo.getTronTransId());
        List<TPaymentRecords> tPaymentRecords = this.baseMapper.tPaymentRecordsList(tPaymentRecordsVo);
        TPaymentRecordsResult tPaymentRecordsResult = new TPaymentRecordsResult();
        if (tPaymentRecords.isEmpty()){
            tPaymentRecordsResult.setList(new ArrayList<>());
        }
        tPaymentRecordsResult.setList(tPaymentRecords);
        tPaymentRecordsResult.setCount(count);
        return tPaymentRecordsResult;
    }

    @Override
    public List<TPaymentRecords> getTPaymentRecordsList() {
        LambdaQueryWrapper<TPaymentRecords> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TPaymentRecords::getStatus, 0);
        List<TPaymentRecords> tPaymentRecordsList = this.list(lambdaQueryWrapper);
        long now = System.currentTimeMillis() / 1000;
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        return tPaymentRecordsList.stream()
                .filter(tPaymentRecords -> tPaymentRecords.getExpiryTime().atZone(zoneId).toEpochSecond() <= now)
                .collect(Collectors.toList());
    }


    @Override
    public List<TPaymentRecords> selectTPaymentRecords(List<String> tronTransId, String userTGID) {
        return this.baseMapper.selectTPaymentRecords(tronTransId,userTGID);
    }

    public List<TPaymentRecords> handlePaymentRecordsExpire() {
        LambdaQueryWrapper<TPaymentRecords> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TPaymentRecords::getStatus, 4);
        List<TPaymentRecords> tPaymentRecordsList = this.list(lambdaQueryWrapper);
        long now = System.currentTimeMillis() / 1000;
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        // 按发送人、领取人和 chatBotName 分组
        Map<String, List<TPaymentRecords>> groupedBySenderReceiverAndChatBot = tPaymentRecordsList.stream()
                .collect(Collectors.groupingBy(tPaymentRecords ->
                        tPaymentRecords.getSendUserId() + ":" + tPaymentRecords.getPaymentUserId() + ":" + tPaymentRecords.getChatBotName()));

        // 在每个分组中取最新的记录
        List<TPaymentRecords> latestRecords = groupedBySenderReceiverAndChatBot.values().stream()
                .map(records -> records.stream()
                        .sorted(Comparator.comparing(TPaymentRecords::getPaymentTime).reversed())
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 过滤出订阅过期时间小于或等于当前时间且 chatBotName 有值的记录
        List<TPaymentRecords> filteredList = latestRecords.stream()
                .filter(tPaymentRecords -> tPaymentRecords.getSubscriptionExpiryTime() != null
                        && tPaymentRecords.getSubscriptionExpiryTime().atZone(zoneId).toEpochSecond() <= now
                        && tPaymentRecords.getChatBotName() != null)
                .collect(Collectors.toList());

        return filteredList;
    }

    /*@Override
    public void handleExpiredSubscriptions() {


       log.info("开始处理订阅到期用户");
        *//*
         * 1、获取付款成功，并且订阅已到期的用户
         *//*
        List<TPaymentRecords> tPaymentRecords = handlePaymentRecordsExpire();

        *//*
         * 2、处理过期的用户，状态修改为已过期--- 因用户可能会出现一个条件多个付款记录，导致收款未到期用户被踢出群组/频道
         *//*
        handlePaymentExpiredSubscriptions();
        *//*
         * 3、获取付款成功，并且已到期的用户---普通收款
         *//*
        if (tPaymentRecords.isEmpty()){
            log.info("handleExpiredSubscriptions 没有需要处理订阅到期的用户");
            return;
        }
        List<String> list = tPaymentRecords.stream()
                .map(TPaymentRecords::getPaymentUserId)
                .collect(Collectors.toList());
        log.info("handleExpiredSubscriptions 需要处理的用户数量为:{}", list.size());
        List<TUser> tUsers = tUserService.queryByUserIdList(list);

        Map<String, TUser> topUserMap =
                tUsers.stream().collect(Collectors.toMap(TUser::getUserId, user -> user));
        log.info("handleExpiredSubscriptions 获取到的用户数量为:{}", tUsers.size());
        *//**
         * 4、开始将用户踢出群聊或者频道
         *//*
        HashMap<String, Object> map = new HashMap<>();
        Integer payment_status_5 = Integer.parseInt(sysDictDataService.getDictValue(PAYMENT_STATUS, PAYMENT_STATUS_5));
        for (TPaymentRecords paymentRecords : tPaymentRecords) {
            //将用户踢出群聊或者频道
            try {
                if (paymentRecords.getChatBotName()!=null && StringUtils.isNotBlank(paymentRecords.getChatBotName())){
                    TUser tUser = topUserMap.get(paymentRecords.getPaymentUserId());
                    map.put("chatName", paymentRecords.getChatBotName());
                    map.put("tronTransId", paymentRecords.getTronTransId());
                    map.put("lang", tUser.getUserId());

                    sendTGMessage(tUser.getTgId(), tUser.getNick(), tUser.getName(), "/handleExcludeUser", map);
                }
                //修改状态为订阅已过期
                updatePaymentRecordToExpired(paymentRecords.getId(), payment_status_5);
            } catch (Exception e) {
                log.error(String.format("将用户踢出群聊或者频道失败,handleExpiredSubscriptions=%s,具体失败信息:", JSONUtil.toJsonStr(paymentRecords)), e);
                continue;
            }
        }
        log.info("结束处理订阅到期用户");
    }*/

    private void handlePaymentExpiredSubscriptions() {
        LambdaQueryWrapper<TPaymentRecords> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TPaymentRecords::getStatus, 4);
        List<TPaymentRecords> tPaymentRecordsList = this.list(lambdaQueryWrapper);
        long now = System.currentTimeMillis() / 1000;
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        List<TPaymentRecords> collect = tPaymentRecordsList.stream()
                .filter(tPaymentRecords -> tPaymentRecords.getSubscriptionExpiryTime() != null && tPaymentRecords.getSubscriptionExpiryTime().atZone(zoneId).toEpochSecond() <= now)
                .collect(Collectors.toList());
        // 更新状态
        collect.forEach(record -> {
            updatePaymentRecordToExpired(record.getId(),5);// 更新数据库中的记录
        });
    }

    @Override
    public void modifyTPaymentRecordsStatusAndSubscriptionExpiryTime(Long id, Integer paymentStatus) {
        TPaymentRecords paymentRecords = new TPaymentRecords();
        paymentRecords.setId(id);
        paymentRecords.setStatus(paymentStatus);
        paymentRecords.setSubscriptionExpiryTime(LocalDateTime.now());
        paymentRecords.setUpdateTime(LocalDateTime.now());
        this.updateById(paymentRecords);
    }

    @Override
    public String getPaymentRecords(String paymentId) {
        return this.baseMapper.getPaymentRecords(paymentId);
    }

    private long toEpochSeconds(LocalDateTime dateTime, ZoneId zoneId) {
        return dateTime.atZone(zoneId).toEpochSecond();
    }

    public List<TPaymentRecords> getPaymentRecord() {
        /*LambdaQueryWrapper<TPaymentRecords> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TPaymentRecords::getIsRemind, 0);
        lambdaQueryWrapper.ne(TPaymentRecords::getChatBotName, null); // 确保 getChatBotName 不为空
        List<TPaymentRecords> tPaymentRecordsList = this.list(lambdaQueryWrapper);*/

        List<TPaymentRecords> paymentRecord = this.baseMapper.getPaymentRecord();
        long now = System.currentTimeMillis() / 1000;
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        return paymentRecord.stream()
                .filter(tPaymentRecords -> {
                    LocalDateTime subscriptionExpiryTime = tPaymentRecords.getSubscriptionExpiryTime();
                    if (subscriptionExpiryTime == null) {
                        return false;
                    }
                    long creationTimeInSeconds = toEpochSeconds(tPaymentRecords.getCreateTime(), zoneId);
                    long expiryTimeInSeconds = toEpochSeconds(subscriptionExpiryTime, zoneId);
                    boolean shouldRemind = (expiryTimeInSeconds - creationTimeInSeconds > FORTY_EIGHT_HOURS_IN_SECONDS) &&
                            (expiryTimeInSeconds - now <= FORTY_EIGHT_HOURS_IN_SECONDS);
                    return shouldRemind;
                })
                .collect(Collectors.toList());
    }

    /*@Override
    public void handleUserBeforeSubscriptionExpires() {
        log.debug("开始处理订阅快过期时，提前48小时提醒用户");
        long start = System.currentTimeMillis();
        *//**
         * 1、获取需提醒的用户列表
         *//*
        List<TPaymentRecords> paymentRecords = getPaymentRecord();

        if (paymentRecords.isEmpty()) {
            log.info("handleUserBeforeSubscriptionExpires 没有需要提醒的用户");
            return;
        }
        *//**
         * 2、将过期的付款记录修改状态并且把扣除的金额返回到收款用户中
         *//*
        List<String> list = paymentRecords.stream()
                .map(TPaymentRecords::getPaymentUserId)
                .collect(Collectors.toList());
        log.info("handleUserBeforeSubscriptionExpires 需要处理的用户数量为:{}", list.size());
        List<TUser> tUsers = tUserService.getTUserByUserIds(list);

        Map<String, TUser> topUserMap =
                tUsers.stream().collect(Collectors.toMap(TUser::getUserId, user -> user));
        for (TPaymentRecords tPaymentRecord : paymentRecords) {
            //修改状态为已过期
            try {
                TUser tUser = topUserMap.get(tPaymentRecord.getPaymentUserId());
                Map<String, Object> map = new HashMap<>();
                map.put("tronTransId", tPaymentRecord.getTronTransId());
                map.put("lang", tUser.getLanguage());
                sendTGMessage(tUser.getTgId(), tUser.getNick(), tUser.getName(), "/handleUserBeforeSubscriptionExpires", map);
                modifyTPaymentRecordsIsRemind(tPaymentRecord.getId(), 1);
            } catch (Exception e) {
                log.error(String.format("处理订阅快过期时，提前48小时提醒用户失败,tPaymentRecord=%s,具体失败信息:", JSONUtil.toJsonStr(tPaymentRecord)), e);
                continue;
            }
        }
        log.debug("结束处理订阅快过期时，提前48小时提醒用户");
        long end = System.currentTimeMillis();
        log.debug("handleUserBeforeSubscriptionExpires 结束处理订阅快过期时，提前48小时提醒用户 耗时:{}",end-start);
    }*/

    private void modifyTPaymentRecordsIsRemind(Long id, int isRemind) {
        TPaymentRecords paymentRecords = new TPaymentRecords();
        paymentRecords.setId(id);
        paymentRecords.setIsRemind(isRemind);
        paymentRecords.setUpdateTime(LocalDateTime.now());
        this.updateById(paymentRecords);
    }


    private void sendTGMessage(String tgId, String name, String nick, String httpName, Map<String, Object> paramMap) {
        CompletableFuture.runAsync(() -> {
            log.info("SendMessageRobotService.sendTGMessage 发送TG消息,tgId={},name={},nick={},httpName={},paramMap={}", tgId, name, nick, httpName, JSONUtil.toJsonStr(paramMap));
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
