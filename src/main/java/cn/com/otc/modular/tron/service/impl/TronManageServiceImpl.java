package cn.com.otc.modular.tron.service.impl;

import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.common.config.NacosConstant;
import cn.com.otc.common.constants.CommonConstant;
import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.redis.RedisOperate;
import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.*;
import cn.com.otc.modular.dict.service.SysDictDataService;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.*;
import cn.com.otc.modular.sys.service.*;
import cn.com.otc.modular.tron.dto.bean.TransferChargeData;
import cn.com.otc.modular.tron.dto.bean.TransferTRXData;
import cn.com.otc.modular.tron.dto.bean.TransferUSDTData;
import cn.com.otc.modular.tron.dto.bean.WithdrawUSDTData;
import cn.com.otc.modular.tron.dto.bean.result.ChargeQrcodeResult;
import cn.com.otc.modular.tron.dto.bean.result.CollectTronResult;
import cn.com.otc.modular.tron.dto.bean.result.TgBotUserResult;
import cn.com.otc.modular.tron.dto.bean.vo.TronWithdrawMoneyVO;
import cn.com.otc.modular.tron.dto.bean.vo.UserBuyPremiumVO;
import cn.com.otc.modular.tron.dto.response.TronTRXResponse;
import cn.com.otc.modular.tron.dto.response.TronUSDTResponse;
import cn.com.otc.modular.tron.dto.vo.result.TronTransResult;
import cn.com.otc.modular.tron.qrcode.BitMatrixBuilder;
import cn.com.otc.modular.tron.qrcode.QRBarCodeUtil;
import cn.com.otc.modular.tron.service.TronManageService;
import cn.com.otc.modular.tron.util.TronUtils;
import cn.hutool.core.net.NetUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @description:充值、提现、收款
 * @author: zhangliyan
 * @time: 2024/2/28
 */
@Slf4j
@Service
public class TronManageServiceImpl implements TronManageService {
    @Resource
    private CommonUtil commonUtil;
    @Autowired
    private TAccountTradeService tAccountTradeService;
    @Autowired
    private TUserService tUserService;
    @Autowired
    private TAccountService tAccountService;
    @Autowired
    private SysDictDataService sysDictDataService;
    @Autowired
    private TTronTransService tTronTransService;
    @Autowired
    private TWalletPoolService tWalletPoolService;
    @Autowired
    private TChargeService tChargeService;
    @Autowired
    private TUserWithdrawalService tUserWithdrawalService;
    @Autowired
    private TAntwalletConfigService tAntwalletConfigService;
    @Autowired
    private TInvitedUserService tInvitedUserService;
    @Autowired
    private TUserWithdrawalTjService tUserWithdrawalTjService;
    @Autowired
    private TUserBuyPremiumService tUserBuyPremiumService;
    @Autowired
    private TTronCollectRecordService tTronCollectRecordService;
    @Autowired
    private TPaymentRecordsService tPaymentRecordsService;
    @Autowired
    private TPaymentShareRecordsService tPaymentShareRecordsService;

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private TronUtils tronUtils;
    @Autowired
    private HutoolJWTUtil hutoolJWTUtil;
    @Autowired
    private HttpRequestUtil httpRequestUtil;
    @Autowired
    private MyCommonConfig myCommonConfig;
    @Autowired
    private RedisOperate redisOperate;
    @Autowired
    private AESUtils aesUtils;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Resource
    private SharedCache userMapLocalCache;

    @Resource
    private NacosConstant nacosConstant;

    private static final String ACCOUNT_TYPE = "account_type";//账户类型
    private static final String TRX = "TRX";
    private static final String USDT = "USDT";


    private static final String TRANS_TYPE = "trans_type";//收款类型
    private static final String AVERAGE_PERSON_MODE = "人均模式";
    private static final String RANDOM_AMOUNT_MODE = "随机金额模式";

    private static final String TRADE_TYPE = "trade_type";
    private static final String TRADE_TYPE_CHARGE = "充值";
    private static final String TRADE_TYPE_COLLECTION = "收款";
    private static final String TRADE_TYPE_WITHDRAWAL = "提现";
    private static final String TRADE_TYPE_INVATED = "邀请返现";
    private static final String TRADE_TYPE_BUY_PREMIUM = "购买会员";
    private static final String TRADE_TYPE_PAYMENT = "付款";
    private static final String TRADE_TYPE_COMMISSION_SHARING = "分享提成";

    private static final String COLLECTION_STATUS = "collection_status";//收款状态
    private static final String COLLECTION_STATUS_0 = "未收款";
    private static final String COLLECTION_STATUS_1 = "已收款";

    private static final String CHARGE_STATUS = "charge_status";//充值状态
    private static final String CHARGE_STATUS_0 = "未充值";
    private static final String CHARGE_STATUS_1 = "已充值";
    private static final String CHARGE_STATUS_2 = "已过期";
    private static final String CHARGE_STATUS_3 = "已取消";

    private static final String WITHDRAWAL_TRX_RATE = "WITHDRAWAL_TRX_RATE";
    private static final String WITHDRAWAL_USDT_RATE = "WITHDRAWAL_USDT_RATE";
    private static final String WITHDRAWAL_USDT_TON_RATE = "WITHDRAWAL_USDT_TON_RATE";
    private static final String INVITED_SHARE_RATE = "INVITED_SHARE_RATE";

    private static final int check_withdrawal_status_0 = 0;//未审核
    private static final int check_withdrawal_status_1 = 1;//审核通过,提现进行中
    private static final int check_withdrawal_status_2 = 2;//提现审核失败
    private static final int check_withdrawal_status_3 = 3;//审核通过,提现成功
    private static final int check_withdrawal_status_4 = 4;//审核通过,提现失败


    private static final String PREMIUM_TYPE = "premium_type";//会员类型
    private static final String PREMIUM_TYPE_0 = "3个月";
    private static final String PREMIUM_TYPE_1 = "6个月";
    private static final String PREMIUM_TYPE_2 = "1年";

    /**
     * 会员类型
     */
    private static final String PREMIUM_TYPE_3_MONTH_PRICE = "PREMIUM_TYPE_3_MONTH_PRICE";
    private static final String PREMIUM_TYPE_6_MONTH_PRICE = "PREMIUM_TYPE_6_MONTH_PRICE";
    private static final String PREMIUM_TYPE_1_YEAR_PRICE = "PREMIUM_TYPE_1_YEAR_PRICE";

    /**
     * 归集阈值
     */
    private static final String COLLECT_TRON_AMOUNT_TRX_LIMIT = "COLLECT_TRON_AMOUNT_TRX_LIMIT";
    private static final String COLLECT_TRON_AMOUNT_USDT_LIMIT = "COLLECT_TRON_AMOUNT_USDT_LIMIT";

    /**
     * 归集状态
     */
    private static int COLLECT_TRON_STATUS_100 = 100; //归集进行中
    private static int COLLECT_TRON_STATUS_200 = 200; //归集成功
    private static int COLLECT_TRON_STATUS_301 = 301; //没有达到阈值
    private static int COLLECT_TRON_STATUS_302 = 302; //宽带或者能量不满足本次转账交易
    private static int COLLECT_TRON_STATUS_303 = 303; //归集错误
    private static int COLLECT_TRON_STATUS_400 = 400; //归集失败

    private static final String BLCOKCHAIN_TYPE = "blockchain_type";//账户类型
    private static final String BLCOKCHAIN_TYPE_TRON = "TRON";
    private static final String BLCOKCHAIN_TYPE_TON = "TON";


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
     * 付款状态 0,已付款 1,申述中 2,申述成功 3,申述失败 4,付款成功 5、订阅已过期
     */
    private static final String PAYMENT_STATUS = "payment_status";
    private static final String PAYMENT_STATUS_0 = "已付款";
    private static final String PAYMENT_STATUS_1 = "申述中";
    private static final String PAYMENT_STATUS_2 = "申述成功";
    private static final String PAYMENT_STATUS_3 = "申述失败";
    private static final String PAYMENT_STATUS_4 = "付款成功";
    private static final String PAYMENT_STATUS_5 = "订阅已过期";



    private static final String BASE62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_LINK_LENGTH = 20; // 控制短链接的长度

    /**
     * 能量
     */
    private static final String PRICE = "PRICE";



    /**
     * 获取充值的钱包地址二维码
     *
     * @param userTGID
     * @param lang
     * @return
     * @throws IOException
     */
    @Override
    public ResponseEntity<?> getWalletQrCode(String userTGID, String lang) {
        /**
         * 1、判断传递参数是否正确
         */
        if (StringUtils.isBlank(userTGID)) {
            log.error("TronManageService.getWalletQrCode 获取充值的钱包地址二维码失败,非法参数");
            return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1017", lang));
        }
        ChargeQrcodeResult result = null;
        try {
            /**
             * 2、校验充值用户是否存在
             */
            TUser chargeTUser = tUserService.getTUserByTGId(userTGID);
            if (chargeTUser == null) {
                log.warn("TronManageService.getWalletQrCode 获取充值的钱包地址二维码失败,充值用户不存在,充值用户userTGID={}", userTGID);
                return ResponseEntity.failure(ResultCodeEnum.USER_IS_NOT_EXIST.code, I18nUtil.getMessage("4002", lang));
            }

            /**
             * 3、获取用户是否存在有效的钱包地址二维码
             */
            Integer status_0 = Integer.parseInt(sysDictDataService.getDictValue(CHARGE_STATUS, CHARGE_STATUS_0));
            TCharge tCharge = tChargeService.queryCharge(null, chargeTUser.getUserId(), null, null, null, status_0);
            if (tCharge == null) {
                result = new ChargeQrcodeResult();
                result.setUserTGID(userTGID);
                result.setIsExistCharge(false);
                return ResponseEntity.success(result);
            }
            String chargeType = sysDictDataService.getDictLabel(ACCOUNT_TYPE, tCharge.getChargeType().toString());
            result = new ChargeQrcodeResult();
            result.setOrderId(tCharge.getOrderId());
            result.setUserTGID(userTGID);
            result.setBase58CheckAddress(tCharge.getBase58CheckAddress());
            result.setMoney(tCharge.getMoney().concat(chargeType));
            result.setQrCodeImageBase64(tCharge.getQrcodeImage());
            Duration duration = Duration.between(LocalDateTime.now(), tCharge.getExpireTime());
            result.setExpireTime(duration.toMinutes() * 60 * 1000);
            result.setIsExistCharge(true);
        } catch (Exception e) {
            log.error("TronManageService.getWalletQrCode 生成充值的钱包地址二维码失败,userTGID:{},具体失败信息:", userTGID, e);
            return ResponseEntity.failure(ResultCodeEnum.SYSTEM_ERROR_500.code, I18nUtil.getMessage("602", lang));
        }
        return ResponseEntity.success(result);
    }

    /**
     * 生成充值的钱包地址二维码
     *
     * @param userTGID
     * @param accountType
     * @param money
     * @param lang
     * @return
     * @throws IOException
     */
    @Override
    public ResponseEntity<?> createWalletQrCode(String userTGID, String accountType, String money, String lang) {
        log.info("开始生成充值的钱包地址二维码,充值用户userTGID={},充值类型accountType={},充值金额money={}", userTGID, accountType, money);
        /**
         * 1、判断传递参数是否正确
         */
        if (StringUtils.isBlank(userTGID) || StringUtils.isBlank(accountType) || StringUtils.isBlank(money)) {
            log.error("TronManageService.createWalletQrCode-非法参数,充值用户userTGID={},充值类型accountType={},充值金额money={}", userTGID, accountType, money);
            return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1019", lang));
        }

        Integer accountType_ = Integer.parseInt(sysDictDataService.getDictValue(ACCOUNT_TYPE, accountType));
        String key = "tg_charge_address_lock_" + userTGID + "_" + accountType + "_" + money;
        RLock lock = redissonClient.getLock(key);
        ChargeQrcodeResult result = null;
        LocalDateTime now = LocalDateTime.now();
        try {
            //生成充值二维码时加锁 操作很类似Java的ReentrantLock机制
            lock.lock();

            /**
             * 2、充值金额校验
             */
            try {
                BigDecimal chargeMoney = new BigDecimal(money);
                BigDecimal minChargeMoney = new BigDecimal("1");
                if (chargeMoney.compareTo(minChargeMoney) < 0) {
                    log.error("createWalletQrCode-生成充值的钱包地址二维码失败,充值金额小于1,充值用户userTGID={},充值类型accountType={},充值金额money={}", userTGID, accountType, money);
                    return ResponseEntity.failure(ResultCodeEnum.CHARGE_MONEY_LIMIT.code, I18nUtil.getMessage("5006", lang));
                }
            } catch (Exception e) {
                log.error("TronManageService.createWalletQrCode 生成充值的钱包地址二维码失败,充值金额的格式有问题,充值用户userTGID={},充值类型accountType={},充值金额money={}", userTGID, accountType, money);
                throw new RRException(I18nUtil.getMessage("4003", lang), ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
            }

            /**
             * 2、校验充值用户是否存在
             */
            TUser chargeTUser = tUserService.getTUserByTGId(userTGID);
            if (chargeTUser == null) {
                log.error("-createWalletQrCode-充值用户不存在,充值用户userTGID={},充值类型accountType={},充值金额money={}", userTGID, accountType, money);
                return ResponseEntity.failure(ResultCodeEnum.USER_IS_NOT_EXIST.code, I18nUtil.getMessage("5012", lang));
            }

            /**
             * 3、判断是否有未完成的充值
             */
            Integer status_0 = Integer.parseInt(sysDictDataService.getDictValue(CHARGE_STATUS, CHARGE_STATUS_0));
            TCharge tCharge = tChargeService.queryCharge(null, chargeTUser.getUserId(), null, null, null, status_0);
            if (tCharge != null && tCharge.getExpireTime().compareTo(now) > 0) {
                log.error("TronManageService.createWalletQrCode-有未完成的充值订单,充值用户userTGID={},充值类型accountType={},充值金额money={}", userTGID, accountType, money);
                return ResponseEntity.failure(ResultCodeEnum.CHARGE_ADDRESS_IS_USEED.code, I18nUtil.getMessage("5002", lang));
            }

            /**
             * 4、判断充值订单是否已过期,过期的话则修改充值订单状态
             */
            if (tCharge != null && tCharge.getExpireTime().compareTo(now) <= 0) {
                Integer status_2 = Integer.parseInt(sysDictDataService.getDictValue(CHARGE_STATUS, CHARGE_STATUS_2));
                //修改充值订单状态为已过期
                tChargeService.modifyChargeStatus(tCharge.getId(), status_2, null, null, null, null);
            }

            /**
             * 5、随机获取一个可以充值的钱包地址
             */
            String base58CheckAddress = getRandomBase58CheckAddress(accountType_, money);
            if(StringUtils.isBlank(base58CheckAddress)){
                log.error("createWalletQrCode-钱包地址都被占用或者钱包地址不存在,充值用户userTGID={},充值类型accountType={},充值金额money={}", userTGID, accountType, money);
                return ResponseEntity.failure(ResultCodeEnum.CHARGE_ADDRESS_IS_USEED.code, I18nUtil.getMessage("5002", lang));
            }

            /**
             * 6、添加充值到redis
             */
            redisOperate.addChargeToRedis(base58CheckAddress, accountType_, money);

            /**
             * 7、生成钱包地址二维码
             */
            String logoPath = "";
            if (accountType.equals(TRX)) {
                logoPath = myCommonConfig.getCommonLogoTrxPath();
            } else if (accountType.equals(USDT)) {
                logoPath = myCommonConfig.getCommonLogoUsdtPath();
            } else {
                logoPath = myCommonConfig.getCommonLogoDefaultPath();
            }
            String qrCodeImage = createWalletQrCode(base58CheckAddress, logoPath);

            /**
             * 8、生成充值订单
             */
            String orderId = "charge_".concat(UidGeneratorUtil.genId());//根据雪花算法获取红包id
            tChargeService.createCharge(orderId, chargeTUser.getUserId(), base58CheckAddress, accountType_, money, qrCodeImage);

            /**
             * 9、返回前端数据
             */
            result = new ChargeQrcodeResult();
            result.setOrderId(orderId);
            result.setUserTGID(userTGID);
            result.setBase58CheckAddress(base58CheckAddress);
            result.setMoney(money.concat(accountType));
            result.setQrCodeImageBase64(qrCodeImage);
            result.setExpireTime(30 * 60 * 1000l);
            result.setIsExistCharge(true);
            log.info("结束生成充值的钱包地址二维码,充值用户userTGID={},充值类型accountType={},充值金额money={}", userTGID, accountType, money);
        } catch (Exception e) {
            log.error(String.format("TronManageService.createWalletQrCode 生成充值的钱包地址二维码失败,userTGID={%s},充值类型accountType={%s},充值金额money={%s},具体失败信息:", userTGID, accountType, money), e);
                throw new RRException(String.format("TronManageService.createWalletQrCode 生成充值的钱包地址二维码失败,userTGID={%s},充值类型accountType={%s},充值金额money={%s}", userTGID, accountType, money), ResultCodeEnum.SYSTEM_ERROR_500.code);
        } finally {
            lock.unlock();
        }
        return ResponseEntity.success(result);
    }

    @Override
    public void cancelTronCharge(String userTGID, String orderId, String lang) {
        log.info("开始取消充值订单 userTGID={},orderId={}", userTGID, orderId);
        try {
            /**
             * 1、判断充值是否存在
             */
            TCharge tCharge = tChargeService.queryCharge(orderId, null, null, null, null, null);
            if (tCharge == null) {
                log.warn(
                        "TronManageService.cancelTronCharge 取消充值订单失败,该笔充值订单不存在,orderId={}", orderId);
                throw new RRException(I18nUtil.getMessage("5003", lang),
                        ResultCodeEnum.CHARGE_ORDER_IS_NOT_EXIST.code);
            }
            /**
             * 2、判断充值的状态是否允许取消
             */
            String status = sysDictDataService.getDictLabel(CHARGE_STATUS, tCharge.getStatus().toString());
            if (!status.equals(CHARGE_STATUS_0)) {
                log.warn(
                        "TronManageService.cancelTronCharge 取消充值订单失败,该笔充值订单" + status + ",orderId={}", orderId);
                throw new RRException(I18nUtil.getMessage("5004", lang) + status,
                        ResultCodeEnum.CHARGE_ORDER_STATUS_IS_ERROR.code);
            }

            /**
             * 3、判断当前是否取消了5次，取消5次后，当天不允许再取消
             */
            String cancel_charge_redis = redisOperate.getCancelChargeToRedis(userTGID);
            if (StringUtils.isNotBlank(cancel_charge_redis) && Integer.parseInt(cancel_charge_redis) >= 5) {
                log.warn(
                        "TronManageService.cancelTronCharge 取消充值订单失败,取消订单次数超过了5次,orderId={}", orderId);
                throw new RRException(I18nUtil.getMessage("5013", lang),
                        ResultCodeEnum.CHARGE_ORDER_STATUS_IS_ERROR.code);
            }

            /**
             * 4、redis中增加取消的次数
             */
            long time = 86400000 - (System.currentTimeMillis() % 86400000);
            redisOperate.addCancelChargeToRedis(userTGID, time);

            /**
             * 5、修改充值订单状态为已取消
             */
            Integer status_3 = Integer.parseInt(sysDictDataService.getDictValue(CHARGE_STATUS, CHARGE_STATUS_3));
            tChargeService.modifyChargeStatus(tCharge.getId(), status_3, null, null, null, null);

            /**
             * 6、删除充值订单redis
             */
            redisOperate.removeChargeToRedis(tCharge.getBase58CheckAddress(), tCharge.getChargeType(), tCharge.getMoney());

            log.info("结束取消充值订单 userTGID={},orderId={}", userTGID, orderId);

        } catch (Exception e) {
            log.error(String.format("TronManageService.cancelTronCharge 取消充值订单失败,orderId={%s},具体失败信息:", orderId), e);
            if (e instanceof RRException) {
                RRException rRException = (RRException) e;
                throw rRException;
            } else {
                throw new RRException(String.format("TronManageService.cancelTronCharge 取消充值订单失败,orderId={%s}", orderId), ResultCodeEnum.SYSTEM_ERROR_500.code);
            }
        }
    }

    /**
     * 充值
     */
    @Override
    @Transactional
    public void handleTronCharge() {
        log.info("TronManageService.handleTronCharge 开始进行监控充值");
        LocalDateTime startTime = LocalDateTime.now();
        /**结束生成充值的钱包地址二维码
         * 1、获取当前所有的未支付充值订单
         */
        List<TCharge> tChargeList = tChargeService.getCurrChargeDataList(0);
        for (TCharge tCharge : tChargeList) {
            /**
             * 2、判断用户是否存在
             */
            TUser chargeTUser = tUserService.getTUserByUserId(tCharge.getUserId());
            if (chargeTUser == null) {
                log.warn("TronManageService.handleTronCharge tCharge={} 没有查询出对应的用户信息", JSONUtil.toJsonStr(tCharge));
                continue;
            }

            /**
             * 3、判断钱包地址是否存在
             */
            TWalletPool tWalletPool = tWalletPoolService.getTWalletPoolByAddress(tCharge.getBase58CheckAddress());
            if (tWalletPool == null) {
                log.warn("TronManageService.handleTronCharge 充值钱包地址是否存在 charge={},userName={}",
                        JSONUtil.toJsonStr(tCharge), chargeTUser.getName());
                continue;
            }

            /**
             * 4、判断充值订单是否已过期,过期的话则释放钱包地址池和修改充值订单状态
             */
            String charge_redis = redisOperate.getChargeToRedis(tCharge.getBase58CheckAddress(), tCharge.getChargeType(), tCharge.getMoney());
            if (StringUtils.isBlank(charge_redis) || tCharge.getExpireTime().compareTo(startTime) <= 0) {
                log.warn("TronManageService.handleTronCharge orderId={},userId={},address={},startTime={},expireTime={} 充值订单超过30分钟未充值,已过期", tCharge.getOrderId(), tCharge.getUserId(),
                        tCharge.getBase58CheckAddress(), startTime, tCharge.getExpireTime());
                Integer status_2 = Integer.parseInt(sysDictDataService.getDictValue(CHARGE_STATUS, CHARGE_STATUS_2));
                //修改充值订单状态为已过期
                tChargeService.modifyChargeStatus(tCharge.getId(), status_2, null, null, null, null);
                continue;
            }
            try {
                /**
                 * 5、监控充值活动,并增加到虚拟账户上
                 */
                addChargeToAccount(tCharge, chargeTUser);
            } catch (Exception e) {
                log.error("TronManageService.handleTronCharge 监控充值失败,具体失败信息:", e);
                continue;
            }
        }

        log.info("结束监控充值");
    }

    /**
     * 向TG发送收款消息
     *
     * @param transAccountVO
     * @param lang
     * @return
     */
    /*@Override
    public String createOTronTrans(TransAccountVO transAccountVO, UserInfoResult userInfoResult, String lang) {
        *//**
         * 1、判断参数是否合法
         *//*
        if (StringUtils.isBlank(transAccountVO.getAccountType()) || StringUtils.isBlank(transAccountVO.getMoney()) ||
                transAccountVO.getTransNum()==null || StringUtils.isBlank(transAccountVO.getTransType())) {
            log.warn("TronManageService.chooseOTronTransUser 非法参数,具体红包信息:{}",
                    JSONUtil.toJsonStr(transAccountVO));
            throw new RRException(I18nUtil.getMessage("1017", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
        }
        transAccountVO.setMoney(transAccountVO.getMoney().replaceAll("¥", ""));

        *//**
         * 2、校验收款用户是否存在
         *//*
        TUser sendTUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
        if (sendTUser == null) {
            log.warn("TronManageService.chooseOTronTransUser 收款用户不存在,收款用户[{}],"
                    + "具体收款信息:{}", userInfoResult.getFirstName(), JSONUtil.toJsonStr(transAccountVO));
            throw new RRException(I18nUtil.getMessage("4015", lang), ResultCodeEnum.USER_IS_NOT_EXIST.code);
        }

        *//**
         * 3、校验收款类型
         *//*
        String dictLabel = sysDictDataService.getDictLabel(ACCOUNT_TYPE, transAccountVO.getAccountType());
        if (StringUtils.isBlank(dictLabel)) {
            log.warn("TronManageService.chooseOTronTransUser 选择的账户类型不存在,收款用户[{}],"
                    + "具体收款信息:{}", userInfoResult.getFirstName(), JSONUtil.toJsonStr(transAccountVO));
            throw new RRException(I18nUtil.getMessage("4001", lang), ResultCodeEnum.ILLEGAL_ACCOUNT_TYPE.code);
        }


        String dictValue = sysDictDataService.getDictLabel(TRANS_TYPE, transAccountVO.getTransType());
        if (StringUtils.isBlank(dictValue)) {
            log.warn("TronManageService.chooseOTronTransUser 选择的收款类型不存在,收款用户[{}],"
                    + "具体收款信息:{}", userInfoResult.getFirstName(), JSONUtil.toJsonStr(transAccountVO));
            throw new RRException(I18nUtil.getMessage("4021", lang), ResultCodeEnum.ILLEGAL_TRANS_TYPE.code);
        }
        *//**
         * 5、最低金额校验
         *//*
        BigDecimal min_limit_money = new BigDecimal("1");

        BigDecimal money;
        try {
            money = new BigDecimal(transAccountVO.getMoney());
        } catch (Exception e) {
            log.error(String.format("TronManageService.chooseOTronTransUser 红包金额的格式有问题,发送红包用户[%s]," + "具体红包信息:%s", sendTUser.getName(), JSONUtil.toJsonStr(transAccountVO)), e);
            throw new RRException(I18nUtil.getMessage("4003", lang), ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
        }

        if (money.compareTo(min_limit_money) < 0) {
            log.warn("TronManageService.chooseOTronTransUser 收款金额小于" + min_limit_money + ",发送用户[{}]," + "具体收款信息:{}", sendTUser.getName(), JSONUtil.toJsonStr(transAccountVO));
            throw new RRException(I18nUtil.getMessage("1010", lang) + min_limit_money, ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
        }

        *//**
         * 8、判断收款数量
         *//*
        BigDecimal max_limit_redpacketnum = new BigDecimal("200000");
        try {
            BigDecimal redpacketNum = new BigDecimal(transAccountVO.getTransNum());
            if (redpacketNum.compareTo(max_limit_redpacketnum) > 0) {
                log.warn("TronManageService.chooseOTronTransUser 收款数量大于" + max_limit_redpacketnum + ",发送用户[{}]," + "具体收款信息:{}", sendTUser.getName(), JSONUtil.toJsonStr(transAccountVO));
                throw new RRException(I18nUtil.getMessage("4006", lang) + max_limit_redpacketnum, ResultCodeEnum.REDPACKET_NUM_LIMIT.code);
            }
        } catch (Exception e) {
            log.error(String.format("TronManageService.chooseOTronTransUser 验证数量失败,发送用户[%s],数量[%s]" + "具体收款信息:{}",
                    sendTUser.getName(), transAccountVO.getTransNum(), max_limit_redpacketnum, JSONUtil.toJsonStr(transAccountVO)), e);
            throw new RRException(I18nUtil.getMessage("4006", lang), ResultCodeEnum.REDPACKET_NUM_LIMIT.code);
        }


        *//**
         * 9、保存发送收款信息,注意:由于接受收款的用户可能还没有在小程序中创建账号,所以要用GroupTGId作为领取红包的用户，后续领取的红包的时候引导进入小程序
         *//*
        String tronTransId = "tron_trans_".concat(UidGeneratorUtil.genId());//根据雪花算法获取收款id
        log.info("TronManageService.chooseOTronTransUser 开始创建收款记录成功,收款信息: tronTransId={},sendUserId={},accountType={},"
                        + "transType={},transNum={},money={},comment={},paymentExpiryTime={},subscriptionDesc={}", tronTransId, sendTUser.getUserId(),
                transAccountVO.getAccountType(), transAccountVO.getTransType(), transAccountVO.getTransNum(), transAccountVO.getMoney(),
                transAccountVO.getComment(), transAccountVO.getPaymentTimeout(), transAccountVO.getSubscriptionDesc());

        String conditions = null;
        TTronTransConditionsBean transConditions = transAccountVO.getConditions();
        if (transConditions != null && (StringUtils.isNotBlank(transConditions.getChannelBotName()) || StringUtils.isNotBlank(transConditions.getGroupName()))) {
            conditions = JSONUtil.toJsonStr(transConditions);
        }
         *//**
         * 10、判断是否设置了分享比例
         *//*
         if (StringUtils.isNotBlank(transAccountVO.getSharingRatio())){
             //设置了，则需要每当有用户邀请成功后，则可以获得对应的奖励
             BigDecimal sharingRatio = new BigDecimal(transAccountVO.getSharingRatio());
             //比例与金额相乘，获取到分享可得的总金额
             BigDecimal shareMoney = sharingRatio.multiply(money);
             //根据分享可得的总金额再除以收款数量，获取到每个用户的分享奖励金额
         }
        tTronTransService.saveTTronTrans(tronTransId, userInfoResult.getUserTGID(), Integer.parseInt(transAccountVO.getAccountType()), transAccountVO.getMoney(),
                transAccountVO.getTransNum(), Integer.parseInt(transAccountVO.getTransType()), transAccountVO.getGroupsName(),transAccountVO.getChannelName(),transAccountVO.getComment(),
                transAccountVO.getPaymentTimeout(), transAccountVO.getSubscriptionTimeout(), transAccountVO.getSubscriptionDesc(),transAccountVO.getCustomerServiceLink(),transAccountVO.getSharingRatio());

        log.info("TronManageService.chooseOTronTransUser 创建收款记录成功,收款信息: tronTransId={},sendUserId={},accountType={},"
                + "transType={},transNum={},money={},comment={},paymentExpiryTime={},subscriptionDesc={},conditions={}", tronTransId, sendTUser.getUserId(),
                transAccountVO.getAccountType(), transAccountVO.getTransType(), transAccountVO.getTransNum(), transAccountVO.getMoney(),
                transAccountVO.getComment(), transAccountVO.getSubscriptionTimeout(), transAccountVO.getSubscriptionDesc(), conditions);
        *//**
         * 12、保存收款信息到redis中,这样就可以设置收款未领取自动过期
         *//*
        redisOperate.addTronTransToRedis(tronTransId, transAccountVO.getTransNum(), transAccountVO.getPaymentTimeout(), TimeUnit.HOURS);
        *//**
         * 5、通知TG
         *//*
        tronTransId = tronTransId.concat(":").concat(sendTUser.getUserId());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("tronTransId", tronTransId);
        paramMap.put("money", transAccountVO.getMoney().concat(dictLabel));
        paramMap.put("lang", sendTUser.getLanguage());
        CompletableFuture.runAsync(()-> sendTGMessage(userInfoResult.getUserTGID(),userInfoResult.getFirstName(), userInfoResult.getUserName(), "sendOTronTrans",  paramMap));

        String shortLink = null;
        try {
            shortLink = commonUtil.generateShortLink(tronTransId);
        } catch (NoSuchAlgorithmException e) {
            log.info("redpacketId:{}, RedPacketManageService.sendGroupRedPacket 生成短链接异常：{},短链接:{}", tronTransId, e,shortLink);
            throw new RRException(I18nUtil.getMessage("500", lang), ResultCodeEnum.SYSTEM_ERROR_500.code);
        }
        log.info("redpacketId:{}, RedPacketManageService.sendGroupRedPacket 生成短链接成功,短链接:{}", tronTransId, shortLink);
        return shortLink;
    }*/


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

    /**
     * 收款
     */
   /* @Override
    public void handleTronTransAccount(String tronTransId, UserInfoResult userInfoResult, String lang) {
        *//**
         * 1、判断参数是否合法
         *//*
        if (StringUtils.isBlank(tronTransId)) {
            log.warn("TronManageService.handleTronTransAccount 非法参数,userInfoResult={}",
                    JSONUtil.toJsonStr(userInfoResult));
            throw new RRException(I18nUtil.getMessage("1017", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
        }

        *//**
         * 2、判断收款是否存在
         *//*
        TTronTrans tTronTrans = tTronTransService.getTTronTrans(tronTransId);
        if (tTronTrans == null) {
            log.warn("TronManageService.handleTronTransAccount 收款不存在,tronTransId={},userInfoResult={}",
                    tronTransId, JSONUtil.toJsonStr(userInfoResult));
            throw new RRException(I18nUtil.getMessage("8004", lang), ResultCodeEnum.TRONTRANS_IS_NOT_EXIST.code);
        }

        *//**
         * 3、判断是否已付款
         *//*
        Integer status = Integer.parseInt(sysDictDataService.getDictValue(COLLECTION_STATUS, COLLECTION_STATUS_1));
        if (tTronTrans.getStatus().equals(status)) {
            log.warn("TronManageService.handleTronTransAccount 该笔收款已收款,tTronTrans={},userInfoResult={}",
                    JSONUtil.toJsonStr(tTronTrans), JSONUtil.toJsonStr(userInfoResult));
            throw new RRException(I18nUtil.getMessage("8006", lang), ResultCodeEnum.TRONTRANS_IS_NOT_EXIST.code);
        }

        *//**
         * 4、收款金额校验
         *//*
        BigDecimal money;
        try {
            money = new BigDecimal(tTronTrans.getMoney());
        } catch (Exception e) {
            log.error(String.format("TronManageService.handleTronTransAccount 收款金额的格式有问题,,tronTransId=%s,userInfoResult=%s",
                    tronTransId, JSONUtil.toJsonStr(userInfoResult), e));
            throw new RRException(I18nUtil.getMessage("4003", lang), ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
        }

        *//**
         * 5、校验付款用户和收款用户是否存在
         *//*
        TUser payTUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
        if (payTUser == null) {
            log.warn("TronManageService.handleTronTransAccount 付款用户不存在,tTronTrans={},userInfoResult={}",
                    JSONUtil.toJsonStr(tTronTrans), JSONUtil.toJsonStr(userInfoResult));
            throw new RRException(I18nUtil.getMessage("4016", lang), ResultCodeEnum.USER_IS_NOT_EXIST.code);
        }
        TUser collectionTUser = tUserService.getTUserByTGId(tTronTrans.getSendUserId());
        if (collectionTUser == null) {
            log.warn("TronManageService.handleTronTransAccount 收款用户不存在,tTronTrans={},userInfoResult={}",
                    JSONUtil.toJsonStr(tTronTrans), JSONUtil.toJsonStr(userInfoResult));
            throw new RRException(I18nUtil.getMessage("4017", lang), ResultCodeEnum.USER_IS_NOT_EXIST.code);
        }

        *//**
         * 6、校验付款用户和收款用户是否一致
         *//*
    *//*if(tTronTrans.getSendUserId().equals(userInfoResult.getUserTGID())){
      log.error("TronManageService.handleTronTransAccount 付款用户和收款用户一致,tronTransId={},付款用户={},收款用户={}",
          tronTransId,payTUser.getName(),collectionTUser.getName());
      throw new RRException("不可以转账给自己哦!",ResultCodeEnum.TRONTRANS_SENDUSER_RECEIVE_SAME.code);
    }*//*

        *//**
         * 7、校验付款用户的账户金额是否足够
         *//*
        TAccount payTAccount = tAccountService.getAccountByUserIdAndAccountType
                (payTUser.getTgId(), tTronTrans.getAccountType());
        if (payTAccount == null) {
            log.warn("TronManageService.handleTronTransAccount 没有查到付款的账户信息,tronTransId={},付款用户={},收款用户={}",
                    tronTransId, payTUser.getName(), collectionTUser.getName());
            throw new RRException(I18nUtil.getMessage("4004", lang), ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code);
        }
        BigDecimal payTAccount_amount;
        try {
            payTAccount_amount = new BigDecimal(payTAccount.getAmount());
        } catch (Exception e) {
            log.error(String.format("TronManageService.handleTronTransAccount 付款账户金额的格式有问题,tronTransId=%s,付款用户=%s,收款用户=%s,付款账户余额=%s",
                    tronTransId, payTUser.getName(), collectionTUser.getName(), payTAccount.getAmount()), e);
            throw new RRException(I18nUtil.getMessage("4003", lang),
                    ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
        }

        if (payTAccount_amount.compareTo(BigDecimal.ZERO) <= 0 || payTAccount_amount.compareTo(money) < 0) {
            log.warn("TronManageService.handleTronTransAccount 付款账户余额不足,付款用户[{}],收款用户[{}],付款账户余额[{}],"
                    + "具体收款信息:{}", payTUser.getName(), collectionTUser.getName(), payTAccount_amount, JSONUtil.toJsonStr(tTronTrans));
            throw new RRException(I18nUtil.getMessage("1020", lang), ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code);
        }

        *//**
         * 8、校验收款账户
         *//*
        TAccount collectionTAccount = tAccountService.getAccountByUserIdAndAccountType
                (tTronTrans.getSendUserId(), tTronTrans.getAccountType());
        if (collectionTAccount == null) {
            log.warn("TronManageService.handleTronTransAccount 没有查到收款的账户信息,tronTrans={},付款用户={},收款用户={}",
                    JSONUtil.toJsonStr(tTronTrans), payTUser.getName(), collectionTUser.getName());
            throw new RRException(I18nUtil.getMessage("4018", lang), ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code);
        }
        BigDecimal collectionTAccount_amount;
        try {
            collectionTAccount_amount = new BigDecimal(collectionTAccount.getAmount());
        } catch (Exception e) {
            log.error(String.format("TronManageService.handleTronTransAccount 收款账户金额的格式有问题,tronTransId=%s,付款用户=%s,收款用户=%s,收款账户余额=%s",
                    tronTransId, payTUser.getName(), collectionTUser.getName(), collectionTAccount.getAmount(), e));
            throw new RRException(I18nUtil.getMessage("4003", lang),
                    ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
        }

        *//**
         * 9、付款账户扣除对应的金额
         *//*
        log.info("TronManageService.handleTronTransAccount 开始扣除付款账户对应的金额,付款账户id={},扣除金额={},账户金额={}", payTAccount.getId(), money, payTAccount_amount);
        tAccountService.deductAccountMoney(payTAccount.getId(), payTAccount_amount, money);
        log.info("TronManageService.handleTronTransAccount 扣除付款账户余额成功,扣除金额={},账户金额={}", money, payTAccount_amount.subtract(money));


        *//**
         * 10、收款账户增加对应的金额
         *//*
        log.info("TronManageService.handleTronTransAccount 开始收款账户增加对应的金额,收款账户id={},增加金额={},账户金额={}", collectionTAccount.getId(), money,
                collectionTAccount_amount);
        tAccountService.addAccountMoney(collectionTAccount.getId(), collectionTAccount_amount, money);
        log.info("TronManageService.handleTronTransAccount 增加收款账户金额成功,增加金额={},账户金额={}", money,
                collectionTAccount_amount.add(money));

        *//**
         * 11、修改收款的状态为已收款
         *//*
        tTronTransService.modifyTTronTransStatus(tTronTrans.getId(), status);

        Integer tradeType = Integer
                .parseInt(sysDictDataService.getDictValue(TRADE_TYPE, TRADE_TYPE_COLLECTION));

        *//**
         * 13、异步保存付款用户交易记录
         *//*
        String pay_trade_money = "-".concat(money.toString());
        BigDecimal pay_amount_new = payTAccount_amount.subtract(money);
        tAccountTradeService
                .saveAccountTrade(payTAccount.getAccountId(), payTUser.getUserId(), tTronTrans.getTronTransId(), pay_trade_money,
                        tradeType, pay_amount_new.toString(), tTronTrans.getAccountType(), null);

        *//**
         * 12、异步保存收款用户交易记录
         *//*
        String trade_money = "+".concat(money.toString());
        BigDecimal amount_new = collectionTAccount_amount.add(money);
        tAccountTradeService
                .saveAccountTrade(collectionTAccount.getAccountId(), collectionTUser.getUserId(), tTronTrans.getTronTransId(), trade_money,
                        tradeType, amount_new.toString(), tTronTrans.getAccountType(),null );
    }*/

    /**
     * 提现
     */
    @Override
    //@Transactional
    public ResponseEntity<?> applyTronWithdrawMoney(TronWithdrawMoneyVO tronWithdrawMoneyVO, UserInfoResult userInfoResult, String lang) {
        log.info("开始申请提现 tronWithdrawMoneyVO={},userInfoResult={}",
                JSONUtil.toJsonStr(tronWithdrawMoneyVO), JSONUtil.toJsonStr(userInfoResult));
        //入参判断
        ResponseEntity<Object> code = judgmentParms(tronWithdrawMoneyVO, userInfoResult, lang);
        if (code != null) return code;

        tronWithdrawMoneyVO.setAccountType("1");//USDT
        /**
         * 1、判断参数是不是非法
         */
        if (
                StringUtils.isBlank(tronWithdrawMoneyVO.getMoney())) {
            return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code,I18nUtil.getMessage("1017", lang));
        }

        if (!("2".equals(tronWithdrawMoneyVO.getBlockchainType()) || "3".equals(tronWithdrawMoneyVO.getBlockchainType())) && StringUtils.isBlank(tronWithdrawMoneyVO.getHexAddress())) {
            log.info("TronManageService.handleTronWithdrawMoney-参数:HexAddress 不能为空,userInfoResult={}", userInfoResult.getUserTGID());
            return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code,I18nUtil.getMessage("1017", lang));
        }

        /**
         * 2、根据TGID获取用户信息
         */
        TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
        if (tUser == null) {
            return ResponseEntity.failure(ResultCodeEnum.USER_IS_NOT_EXIST.code,I18nUtil.getMessage("4015", lang));
        }

        //加锁
        String lockKey = "applyTronWithdrawMoney_lock_" + userInfoResult.getUserTGID();
        RLock lock = redissonClient.getLock(lockKey);
        String withdrawId = null;//根据雪花算法获取提现id
        TransactionStatus status = null; // 用于保存事务状态
        try {
            log.info("applyTronWithdrawMoney--Lock acquired by thread [{}] for key: {}", Thread.currentThread().getName(), lockKey);
            lock.lock();
            TUserWithdrawal tUserWithdrawal = tUserWithdrawalService.getTUserWithdrawalByStatus(tUser.getUserId(), tronWithdrawMoneyVO.getHexAddress(),
                    1, check_withdrawal_status_0);
            if (tUserWithdrawal != null) {
                log.warn("TronManageService.handleTronWithdrawMoney 有提现的申请还没有审核,tronWithdrawMoneyVO={},userInfoResult={}",
                        JSONUtil.toJsonStr(tronWithdrawMoneyVO), JSONUtil.toJsonStr(userInfoResult));
                return ResponseEntity.failure(ResultCodeEnum.USER_IS_NOT_EXIST.code,I18nUtil.getMessage("5014", lang));
            }

            BigDecimal money = new BigDecimal(tronWithdrawMoneyVO.getMoney());
            // 开启事务
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED); // 设置事务传播行为
            status = transactionManager.getTransaction(def); // 开始事务
            /**
             * 3、获取提现手续费
             */
            String withdrawalRate = "";
            if ("2".equals(tronWithdrawMoneyVO.getBlockchainType())) {
                //3.1最小提现金额的判断
                String inrPrice = redisOperate.getRedis("INR_PRICE");
                BigDecimal exchangeRate = new BigDecimal(inrPrice);
                //换算成印度的价格
                BigDecimal exchangeMoney = money.multiply(exchangeRate);
                //印度提现最少是185
                if (exchangeMoney.compareTo(nacosConstant.getIndianMinimumWithdrawalAmount())<0){
                    return ResponseEntity.failure(ResultCodeEnum.WITHDRAWAL_NOT_ENOUGH.code,I18nUtil.getMessage("9001", lang));
                }

                //3.2 手续费计算
                //手续费= 提现金额*手续费比例
                BigDecimal multiply = new BigDecimal(tronWithdrawMoneyVO.getMoney()).multiply(nacosConstant.getIndianPamentRate());
                //手续费换算成印度卢比
                multiply = multiply.multiply(exchangeRate);
                if (multiply.compareTo(nacosConstant.getIndianPamentMinFee()) < 0) {
                    multiply = nacosConstant.getIndianPamentMinFee();
                }
                //手续费再换算成usdt
                multiply = multiply.divide(exchangeRate, 2, RoundingMode.HALF_UP);
                withdrawalRate = multiply.toString();
                tronWithdrawMoneyVO.setBankName("Indian Bank");

            } else if ("3".equals(tronWithdrawMoneyVO.getBlockchainType())) {
                //3.1最小提现金额的判断
                String phpPrice = redisOperate.getRedis("PHP_PRICE");
                BigDecimal exchangeRate = new BigDecimal(phpPrice);
                //换算成php的价格
                BigDecimal exchangeMoney = money.multiply(exchangeRate);
                //菲律宾提现最少是160
                if (exchangeMoney.compareTo(nacosConstant.getPhilippinesMinimumWithdrawalAmount())<0){
                    return ResponseEntity.failure(ResultCodeEnum.WITHDRAWAL_NOT_ENOUGH.code,I18nUtil.getMessage("9001", lang));
                }

                //3.2 手续费计算
                BigDecimal multiply = new BigDecimal(tronWithdrawMoneyVO.getMoney()).multiply(nacosConstant.getPhilippinesPamentRate());
                //手续费换算php的价格
                multiply=multiply.multiply(exchangeRate);
                if (multiply.compareTo(nacosConstant.getPhilippinesPamentMinFee()) < 0) {
                    multiply = nacosConstant.getPhilippinesPamentMinFee();
                }
                //手续费再换算成usdt
                multiply = multiply.divide(exchangeRate, 2, RoundingMode.HALF_UP);
                withdrawalRate = multiply.toString();

            } else {
                TAntwalletConfig withdrawal_usdt_rax = tAntwalletConfigService.getById(WITHDRAWAL_USDT_RATE);
                TAntwalletConfig withdrawal_usdt_ton_rax = tAntwalletConfigService.getById(WITHDRAWAL_USDT_TON_RATE);

                //blockchainType 链类型 0,TRON 1,TON 2：印度支付，3：菲律宾支付
                if (tronWithdrawMoneyVO.getBlockchainType().equals("0")) {
                    withdrawalRate = withdrawal_usdt_rax.getPValue();
                } else if (tronWithdrawMoneyVO.getBlockchainType().equals("1")) {
                    withdrawalRate = withdrawal_usdt_ton_rax.getPValue();
                } else {
                    withdrawalRate = withdrawal_usdt_rax.getPValue();
                }

            }


            /**
             * 4、判断账户中是否有足够的金额体现
             */

            TAccount tAccount = tAccountService.getAccountByUserIdAndAccountType(tUser.getTgId(), 1);
            if (tAccount == null) {
                log.warn("TronManageService.handleTronWithdrawMoney userId={},accountType={} 没有查到用户的账户信息", tUser.getUserId(), tronWithdrawMoneyVO.getAccountType());
                return ResponseEntity.failure(ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code, I18nUtil.getMessage("4004", lang));
            }
            BigDecimal amount = new BigDecimal(tAccount.getAmount());
            if (amount.compareTo(money) < 0) {
                log.warn("TronManageService.handleTronWithdrawMoney userId={},accountType={},amount={},money={} 没有足够金额提现",
                        tUser.getUserId(), tronWithdrawMoneyVO.getAccountType(), amount, money);
                return ResponseEntity.failure(ResultCodeEnum.WITHDRAWAL_NOT_ENOUGH.code,I18nUtil.getMessage("9001", lang));
            }



            /**
             * 5、获取实际提现数量
             */
            BigDecimal withdrawalRate_dec = new BigDecimal(withdrawalRate);
            BigDecimal withdrawalMoney = money.subtract(withdrawalRate_dec);
            BigDecimal value = new BigDecimal("0.1");
            if (withdrawalMoney.compareTo(value) < 0) {
                log.warn("TronManageService.handleTronWithdrawMoney 提现金额必须大于或等于{},tronWithdrawMoneyVO={},userInfoResult={}",
                        withdrawalRate_dec.add(value).toString().concat(tronWithdrawMoneyVO.getAccountType()), JSONUtil.toJsonStr(tronWithdrawMoneyVO), JSONUtil.toJsonStr(userInfoResult));
                return ResponseEntity.failure(ResultCodeEnum.WITHDRAWAL_NOT_ENOUGH.code, I18nUtil.getMessage("9003", lang) + withdrawalRate_dec.add(value).toString().concat(tronWithdrawMoneyVO.getAccountType()));
            }


            /**
             * 6、开始申请提现
             */
            withdrawId = "withdraw_".concat(UidGeneratorUtil.genId());
            tUserWithdrawalService.saveTUserWithdrawal(withdrawId, tUser.getUserId(), tronWithdrawMoneyVO.getHexAddress(), Integer.valueOf(tronWithdrawMoneyVO.getBlockchainType()),
                    1, tronWithdrawMoneyVO.getMoney(), withdrawalMoney.toString(), withdrawalRate
                    ,tronWithdrawMoneyVO.getBankName(),tronWithdrawMoneyVO.getBankCardName(),tronWithdrawMoneyVO.getBankCardAccount()
            ,tronWithdrawMoneyVO.getIFSC(),"",tronWithdrawMoneyVO.getPhilippinesWithdrawalType());

            /**
             * 7、提现账户扣除对应的金额
             */
            BigDecimal amount_new = amount.subtract(money);
            log.info("TronManageService.handleTronWithdrawMoney 开始扣除提现账户对应的金额,提现账户id={},扣除金额={},账户金额={}", tAccount.getAccountId(), money, amount);
            tAccountService.deductAccountMoney(tAccount.getId(), amount, money);
            log.info("TronManageService.handleTronWithdrawMoney 扣除提现账户余额成功,扣除金额={},账户金额={}", money, amount_new);

            /**
             * 8、异步保存用户提现交易记录
             */
            Integer tradeType = Integer.parseInt(sysDictDataService.getDictValue(TRADE_TYPE, TRADE_TYPE_WITHDRAWAL));
            String pay_trade_money = "-".concat(money.toString());
            tAccountTradeService
                    .saveAccountTrade(tAccount.getAccountId(), tUser.getUserId(), withdrawId, pay_trade_money,
                            tradeType, amount_new.toString(), 1,null);
            // 提交事务
            transactionManager.commit(status);

            /**
             * 9、机器人提醒提现信息
             */
            String finalWithdrawId = withdrawId;
            CompletableFuture.runAsync(() -> HttpRequest.get("https://api.telegram.org/bot" + myCommonConfig.getTgHttpTgToken()
                            + "/sendMessage?chat_id=-1002268855454&text=" + nacosConstant.getEnvType() + userInfoResult.getUserName() + "申请提现!tgId:" + userInfoResult.getUserTGID() + ",withdrawId:" + finalWithdrawId)
                    .execute().body());

            log.info("TronManageService.handleTronWithdrawMoney 结束申请提现,userInfoResult={},withdrawId={}", JSONUtil.toJsonStr(userInfoResult), withdrawId);
            return ResponseEntity.success("Withdrawal request successful. Please wait for approval！");
        } catch (Exception e) {
            log.error("扣除提现账户金额时发生错误: ", e);
            // 回滚事务
            if (status != null) {
                // 回滚事务
                transactionManager.rollback(status);
            }
        }finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("lockKey：{}，释放锁", lockKey);
            }
        }
        return ResponseEntity.failure("withdrawal fail！");
    }

    private static ResponseEntity<Object> judgmentParms(TronWithdrawMoneyVO tronWithdrawMoneyVO, UserInfoResult userInfoResult, String lang) {
        if ("2".equals(tronWithdrawMoneyVO.getBlockchainType())) {
            if (StringUtils.isBlank(tronWithdrawMoneyVO.getBankCardName())) {
                log.info("tgID{},TronManageService.applyTronWithdrawMoney-收款人姓名不能为空", userInfoResult.getUserTGID());
                return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1017", lang));
            }
            if (StringUtils.isBlank(tronWithdrawMoneyVO.getBankCardAccount())) {
                log.info("tgID{},TronManageService.applyTronWithdrawMoney-收款人账号不能为空", userInfoResult.getUserTGID());
                return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1017", lang));
            }
            if (StringUtils.isBlank(tronWithdrawMoneyVO.getIFSC())) {
                log.info("tgID{},TronManageService.applyTronWithdrawMoney-支行不能为空", userInfoResult.getUserTGID());
                return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1017", lang));
            }
        }
        if ("3".equals(tronWithdrawMoneyVO.getBlockchainType())) {
            if (StringUtils.isBlank(tronWithdrawMoneyVO.getBankCardAccount())) {
                log.info("tgID{},judgmentParms-收款人账号不能为空", userInfoResult.getUserTGID());
                return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1017", lang));
            }else {
                tronWithdrawMoneyVO.setBankCardName(tronWithdrawMoneyVO.getBankCardAccount());
                tronWithdrawMoneyVO.setBankName(tronWithdrawMoneyVO.getBankCardAccount());
            }
            if (StringUtils.isBlank(tronWithdrawMoneyVO.getPhilippinesWithdrawalType())){
                log.info("tgID{},judgmentParms-菲律宾提现方式不能为空", userInfoResult.getUserTGID());
                return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1017", lang));
            }
        }
        return null;
    }

    // 机器人提醒提现信息
    private void reminderMessage(UserInfoResult userInfoResult, String tgId, String messageId) {
        String token = createSendMessageToken(userInfoResult.getUserTGID(), userInfoResult.getFirstName(), userInfoResult.getUserName());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("tgId", tgId);
        paramMap.put("messageId", messageId);
        httpRequestUtil.doGetRequest(myCommonConfig.getTgHttpUrl().concat("/reminderMessage"), token, paramMap, myCommonConfig.getTgHttpTimeOut());
    }

    /**
     * 用户提现手续费统计
     */
   /* @Override
    public void handleUserWithdrawalTj() {
        log.info("TronManageService.handleUserWithdrawalTj 开始处理用户提现手续费统计");
        List<TUserWithdrawal> tUserWithdrawalList = tUserWithdrawalService.getCurrCheckOkTUserWithdrawalList();
        if (tUserWithdrawalList == null || tUserWithdrawalList.size() == 0) {
            log.info("TronManageService.handleUserWithdrawalTj 结束处理用户提现手续费统计,没有需要处理的统计");
            return;
        }

        Map<String, String> tInvitedUserMap = new HashMap<>();
        for (TUserWithdrawal tUserWithdrawal : tUserWithdrawalList) {
            TInvitedUser tInvitedUser = tInvitedUserService.getTInvitedUser(tUserWithdrawal.getUserId());
            if (tInvitedUser == null) {
                continue;
            }
            if (!tInvitedUserMap.containsKey(tInvitedUser.getInvitedUserId())) {
                tInvitedUserMap.put(tInvitedUser.getInvitedUserId(), tInvitedUser.getUserId());
            }
        }

        if (tInvitedUserMap == null || tInvitedUserMap.size() == 0) {
            log.info("TronManageService.handleUserWithdrawalTj 结束处理用户提现手续费统计,没有需要处理的统计");
            return;
        }

        //转换map
        Map<String, List<String>> mainUserIdMap = tInvitedUserMap.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));

        //获取邀请用户分享的手续费比例
        TAntwalletConfig invited_share_rate = tAntwalletConfigService.getById(INVITED_SHARE_RATE);
        BigDecimal InvitedShareRate = new BigDecimal(invited_share_rate.getPValue());

        for (Map.Entry<String, List<String>> entry : mainUserIdMap.entrySet()) {
            TUser tUser = tUserService.getTUserByUserId(entry.getKey());
            if (tUser == null) {
                log.warn("TronManageService.handleUserWithdrawalTj tUserId={} 没有查询出对应的用户信息", entry.getKey());
                continue;
            }

            BigDecimal withdrawalTotalMoney = new BigDecimal(BigDecimal.ZERO.toString());
            BigDecimal withdrawalTotalMoneyUsdt = new BigDecimal(BigDecimal.ZERO.toString());
            BigDecimal withdrawalShareMoney = new BigDecimal(BigDecimal.ZERO.toString());
            BigDecimal withdrawalShareMoneyUsdt = new BigDecimal(BigDecimal.ZERO.toString());
            BigDecimal money_trx = new BigDecimal(BigDecimal.ZERO.toString());
            BigDecimal money_usdt = new BigDecimal(BigDecimal.ZERO.toString());
            BigDecimal share_money_trx = new BigDecimal(BigDecimal.ZERO.toString());
            BigDecimal share_money_usdt = new BigDecimal(BigDecimal.ZERO.toString());
            List<String> tInvitedUserIds = entry.getValue();
            for (TUserWithdrawal tUserWithdrawal : tUserWithdrawalList) {
                for (String tInvitedUserId : tInvitedUserIds) {
                    if (!tInvitedUserId.equals(tUserWithdrawal.getUserId())) {
                        continue;
                    }
                    String accountType = sysDictDataService.getDictLabel(ACCOUNT_TYPE, tUserWithdrawal.getAccountType().toString());
                    BigDecimal withdrawalRate = new BigDecimal(tUserWithdrawal.getWithdrawalRate());
                    BigDecimal withdrawalShareMoney_ = withdrawalRate.multiply(InvitedShareRate);
                    if (accountType.equals(TRX)) {
                        money_trx = money_trx.add(withdrawalRate);
                        share_money_trx = share_money_trx.add(withdrawalShareMoney_);
                    } else if (accountType.equals(USDT)) {
                        money_usdt = money_usdt.add(withdrawalRate);
                        share_money_usdt = share_money_usdt.add(withdrawalShareMoney_);
                    }
                }
            }

            withdrawalTotalMoney = withdrawalTotalMoney.add(money_trx);
            withdrawalTotalMoneyUsdt = withdrawalTotalMoneyUsdt.add(money_usdt);
            withdrawalShareMoney = withdrawalShareMoney.add(share_money_trx);
            withdrawalShareMoneyUsdt = withdrawalShareMoneyUsdt.add(share_money_usdt);

            TUserWithdrawalTj tUserWithdrawalTj = tUserWithdrawalTjService.getTUserWithdrawalTj(entry.getKey());
            if (tUserWithdrawalTj == null) {
                tUserWithdrawalTjService.saveTUserWithdrawalTj(entry.getKey(), withdrawalTotalMoney.toString(),
                        withdrawalTotalMoneyUsdt.toString(), withdrawalShareMoney.toString(), withdrawalShareMoneyUsdt.toString());
            } else {
                BigDecimal withdrawalTjTotalMoney = new BigDecimal(tUserWithdrawalTj.getWithdrawalTotalMoney());
                BigDecimal withdrawalTjTotalMoneyUsdt = new BigDecimal(tUserWithdrawalTj.getWithdrawalTotalMoneyUsdt());
                BigDecimal withdrawalTjShareMoney = new BigDecimal(tUserWithdrawalTj.getWithdrawalShareMoney());
                BigDecimal withdrawalTjShareMoneyUsdt = new BigDecimal(tUserWithdrawalTj.getWithdrawalShareMoneyUsdt());
                withdrawalTjTotalMoney = withdrawalTjTotalMoney.add(withdrawalTotalMoney);
                withdrawalTjTotalMoneyUsdt = withdrawalTjTotalMoneyUsdt.add(withdrawalTotalMoneyUsdt);
                withdrawalTjShareMoney = withdrawalTjShareMoney.add(withdrawalShareMoney);
                withdrawalTjShareMoneyUsdt = withdrawalTjShareMoneyUsdt.add(withdrawalShareMoneyUsdt);
                tUserWithdrawalTjService.modifyTUserWithdrawalTj(entry.getKey(), withdrawalTjTotalMoney.toString(),
                        withdrawalTjTotalMoneyUsdt.toString(), withdrawalTjShareMoney.toString(), withdrawalTjShareMoneyUsdt.toString());
            }

            //增加TRX账户金额
            if (money_trx.compareTo(BigDecimal.ZERO) > 0 && share_money_trx.compareTo(BigDecimal.ZERO) > 0) {
                *//**
                 * 获取邀请用户的账户
                 *//*
                Integer accountType = Integer.parseInt(sysDictDataService.getDictValue(ACCOUNT_TYPE, TRX));
                TAccount tAccount = tAccountService
                        .getAccountByUserIdAndAccountType(tUser.getTgId(), accountType);
                if (tAccount != null) {
                    *//**
                     * 邀请用户的账户添加对应的邀请返现金额
                     *//*
                    BigDecimal amount = new BigDecimal(tAccount.getAmount());
                    BigDecimal amount_new = amount.add(share_money_trx);
                    log.info(
                            "TronManageService.handleUserWithdrawalTj 开始增加邀请返现对应的金额,邀请返现账户id={},增加金额={},账户金额={},accountType={}",
                            tAccount.getAccountId(), share_money_trx, amount, TRX);
                    tAccountService.addAccountMoney(tAccount.getId(), amount, share_money_trx);
                    log.info(
                            "TronManageService.handleUserWithdrawalTj 增加邀请返现对应的金额成功,邀请返现账户id={},邀请返现账户id={},增加金额={},账户金额={},accountType={}",
                            tAccount.getAccountId(), share_money_trx, amount_new, amount, TRX);

                    *//**
                     * 异步保存用户提现交易记录
                     *//*
                    Integer tradeType = Integer
                            .parseInt(sysDictDataService.getDictValue(TRADE_TYPE, TRADE_TYPE_INVATED));
                    String trade_money = "+".concat(share_money_trx.toString());
                    tAccountTradeService
                            .saveAccountTrade(tAccount.getAccountId(), entry.getKey(), tAccount.getAccountId(),
                                    trade_money,
                                    tradeType, amount_new.toString(), accountType,null );

                    *//**
                     * 通知TG
                     *//*
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("lang", tUser.getLanguage());
                    paramMap.put("money", share_money_trx.toString().concat(TRX).concat(" | 0").concat(USDT));
                    try {
                        sendTGMessage(tUser.getTgId(), tUser.getName(),
                                tUser.getNick(), "sendUserWithdrawalTj", paramMap);
                    } catch (Exception e) {
                        throw new RRException("邀请返现失败,通知TG失败", ResultCodeEnum.TG_HTTP_ERROR.code);
                    }
                }
            }

            //增加USDT账户金额
            if (money_usdt.compareTo(BigDecimal.ZERO) > 0
                    && share_money_usdt.compareTo(BigDecimal.ZERO) > 0) {
                *//**
                 * 获取邀请用户的账户
                 *//*
                Integer accountType_usdt = Integer
                        .parseInt(sysDictDataService.getDictValue(ACCOUNT_TYPE, USDT));
                TAccount tAccount_usdt = tAccountService
                        .getAccountByUserIdAndAccountType(tUser.getTgId(), accountType_usdt);
                if (tAccount_usdt != null) {
                    *//**
                     * 邀请用户的账户添加对应的邀请返现金额
                     *//*
                    BigDecimal amount = new BigDecimal(tAccount_usdt.getAmount());
                    BigDecimal amount_new = amount.add(share_money_usdt);
                    log.info(
                            "TronManageService.handleUserWithdrawalTj 开始增加邀请返现对应的金额,邀请返现账户id={},增加金额={},账户金额={},accountType={}",
                            tAccount_usdt.getAccountId(), share_money_usdt, amount, USDT);
                    tAccountService.addAccountMoney(tAccount_usdt.getId(), amount, share_money_usdt);
                    log.info(
                            "TronManageService.handleUserWithdrawalTj 增加邀请返现对应的金额成功,邀请返现账户id={},邀请返现账户id={},增加金额={},账户金额={},accountType={}",
                            tAccount_usdt.getAccountId(), share_money_usdt, amount_new, amount, USDT);

                    *//**
                     * 异步保存用户提现交易记录
                     *//*
                    Integer tradeType = Integer
                            .parseInt(sysDictDataService.getDictValue(TRADE_TYPE, TRADE_TYPE_INVATED));
                    String trade_money = "+".concat(share_money_usdt.toString());
                    tAccountTradeService
                            .saveAccountTrade(tAccount_usdt.getAccountId(), entry.getKey(),
                                    tAccount_usdt.getAccountId(), trade_money,
                                    tradeType, amount_new.toString(), accountType_usdt,null );

                    *//**
                     * 通知TG
                     *//*
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("money", "0".concat(TRX).concat(" | ").concat(share_money_usdt.toString()).concat(USDT));
                    paramMap.put("lang", tUser.getLanguage());
                    try {
                        sendTGMessage(tUser.getTgId(), tUser.getName(),
                                tUser.getNick(), "sendUserWithdrawalTj", paramMap);
                    } catch (Exception e) {
                        throw new RRException("邀请返现失败,通知TG失败", ResultCodeEnum.TG_HTTP_ERROR.code);
                    }
                }
            }
        }
        log.info("TronManageService.handleUserWithdrawalTj 结束处理用户提现手续费统计");
    }*/

    @Override
    public ResponseEntity<?> handleUserBuyPremiumBySelf(UserBuyPremiumVO userBuyPremiumVO, UserInfoResult userInfoResult, String lang) {
        log.info("TronManageService.handleUserBuyPremium 开始进行购买会员 userBuyPremiumVO={},userInfoResult={}",
                JSONUtil.toJsonStr(userBuyPremiumVO), JSONUtil.toJsonStr(userInfoResult));
        /**
         * 1、判断参数是不是非法
         */
        if (userBuyPremiumVO.getBuyType() == null || userBuyPremiumVO.getPremiumType() == null) {
            log.warn("TronManageService.handleUserBuyPremium 非法参数,userBuyPremiumVO={},userInfoResult={}",
                    JSONUtil.toJsonStr(userBuyPremiumVO), JSONUtil.toJsonStr(userInfoResult));
            //throw new RRException(I18nUtil.getMessage("1017", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
            return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1017", lang));
        }

        /**
         * 2、判断购买的会员类型是否有效
         */
        String premium_type = sysDictDataService.getDictLabel(PREMIUM_TYPE, String.valueOf(userBuyPremiumVO.getPremiumType()));
        if (StringUtils.isBlank(premium_type)) {
            log.warn("TronManageService.handleUserBuyPremium 购买的会员类型无效,userBuyPremiumVO={},userInfoResult={}",
                    JSONUtil.toJsonStr(userBuyPremiumVO), JSONUtil.toJsonStr(userInfoResult));
            //throw new RRException(I18nUtil.getMessage("10007", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
            return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("10007", lang));
        }
        /**
         * 3、判断输入的用户名是否正确
         */
        String[] split = userBuyPremiumVO.getUserName().split(",");
        for (String s : split) {
            ResponseEntity<?> responseEntity = tUserBuyPremiumService.getChat(s, lang);
            if (0 != responseEntity.getCode()) {
                return responseEntity;
            }
            String name = String.valueOf(responseEntity.getData());
            if (StringUtils.isBlank(name)) {
                log.warn("TronManageService.handleUserBuyPremium 购买的会员的用户无效,userBuyPremiumVO={},userInfoResult={}",
                        JSONUtil.toJsonStr(userBuyPremiumVO), JSONUtil.toJsonStr(userInfoResult));
                //throw new RRException(I18nUtil.getMessage("606", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
                return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("606", lang));
            }
        }



        /**
         * 4、判断用户是为自己购买还是为他人购买 0是为自己购买，1是为他人购买
         */
        if (userBuyPremiumVO.getBuyType() == 0) {
            userBuyPremiumVO.setUserId(userInfoResult.getUserTGID());
            userBuyPremiumVO.setUserName(userInfoResult.getUserName());
        }

        /**
         * 5、获取购买会员的价格
         */
        BigDecimal premium_price = null;
        switch (premium_type) {
            case PREMIUM_TYPE_0:
                TAntwalletConfig premium_type_3_month_price = tAntwalletConfigService.getById(PREMIUM_TYPE_3_MONTH_PRICE);
                premium_price = new BigDecimal(premium_type_3_month_price.getPValue());
                break;
            case PREMIUM_TYPE_1:
                TAntwalletConfig premium_type_6_month_price = tAntwalletConfigService.getById(PREMIUM_TYPE_6_MONTH_PRICE);
                premium_price = new BigDecimal(premium_type_6_month_price.getPValue());
                break;
            case PREMIUM_TYPE_2:
                TAntwalletConfig premium_type_1_year_price = tAntwalletConfigService.getById(PREMIUM_TYPE_1_YEAR_PRICE);
                premium_price = new BigDecimal(premium_type_1_year_price.getPValue());
                break;
            default:
                TAntwalletConfig premium_type_3_month_price_ = tAntwalletConfigService.getById(PREMIUM_TYPE_3_MONTH_PRICE);
                premium_price = new BigDecimal(premium_type_3_month_price_.getPValue());
                break;
        }

        if (premium_type == null) {
            log.warn("TronManageService.handleUserBuyPremium 会员价格出错,userBuyPremiumVO={},userInfoResult={}",
                    JSONUtil.toJsonStr(userBuyPremiumVO), JSONUtil.toJsonStr(userInfoResult));
            //throw new RRException(I18nUtil.getMessage("10001", lang), ResultCodeEnum.PREMIUM_BUY_PRICE_ERROR.code);
            return ResponseEntity.failure(ResultCodeEnum.PREMIUM_BUY_PRICE_ERROR.code, I18nUtil.getMessage("10001", lang));
        }

        /**
         * 5、为多人购买则，需要乘以购买人数
         */
        premium_price = premium_price.multiply(BigDecimal.valueOf(split.length)).setScale(4, RoundingMode.DOWN);

        /**
         * 6、判断账户中是否有足够的金额为自己购买会员或者其他用户购买会员
         */
        Integer accountType = Integer.parseInt(sysDictDataService.getDictValue(ACCOUNT_TYPE, USDT));
        TAccount tAccount = tAccountService.getAccountByUserIdAndAccountType(userInfoResult.getUserTGID(), accountType);
        if (tAccount == null) {
            log.warn("TronManageService.handleUserBuyPremium userId={},accountType={} 没有查到用户的账户信息", userInfoResult.getUserTGID(), accountType);
            //throw new RRException(I18nUtil.getMessage("4004", lang), ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code);
            return ResponseEntity.failure(ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code, I18nUtil.getMessage("4004", lang));
        }
        BigDecimal amount = new BigDecimal(tAccount.getAmount());
        if (amount.compareTo(premium_price) < 0) {
            log.warn("TronManageService.handleUserBuyPremium userId={},accountType={},amount={},money={} 没有足够金额进行购买会员",
                    userInfoResult.getUserTGID(), accountType, amount, premium_price);
            //throw new RRException(I18nUtil.getMessage("10002", lang), ResultCodeEnum.PREMIUM_BUY_NOT_ENOUGH.code);
            return ResponseEntity.failure(ResultCodeEnum.MONEY_NOT_ENOUGH.code, I18nUtil.getMessage("10002", lang));
        }

        /**
         * 7、开始会员购买
         */
        String premiumBuyId = "premium_buy_".concat(UidGeneratorUtil.genId());//根据雪花算法获取提现id
        tUserBuyPremiumService.saveTUserBuyPremium(premiumBuyId, userInfoResult.getUserTGID(), userBuyPremiumVO.getUserId(),
                userBuyPremiumVO.getUserName(), userBuyPremiumVO.getPremiumType(), premium_price.toString(), accountType);

        /**
         * 7、购买会员账户扣除对应的金额
         */
        BigDecimal amount_new = amount.subtract(premium_price);
        log.info("TronManageService.handleUserBuyPremium 开始扣除购买会员账户对应的金额,付款账户id={},扣除金额={},账户金额={}", tAccount.getAccountId(), premium_price, amount);
        tAccountService.deductAccountMoney(tAccount.getId(), amount, premium_price);
        log.info("TronManageService.handleUserBuyPremium 扣除购买会员账户余额成功,扣除金额={},账户金额={}", premium_price, amount_new);

        /**
         * 8、异步保存购买会员交易记录
         */
        Integer tradeType = Integer.parseInt(sysDictDataService.getDictValue(TRADE_TYPE, TRADE_TYPE_BUY_PREMIUM));
        String pay_trade_money = "-".concat(premium_price.toString());
        TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
        tAccountTradeService
                .saveAccountTrade(tAccount.getAccountId(), tUser.getUserId(), premiumBuyId, pay_trade_money,
                        tradeType, premium_price.toString(), accountType,null );

        log.info("TronManageService.handleUserBuyPremium 结束购买会员 userBuyPremiumVO={},userInfoResult={},premiumBuyId={}",
                JSONUtil.toJsonStr(userBuyPremiumVO), JSONUtil.toJsonStr(userInfoResult), premiumBuyId);
        //机器人提醒管理员
        reminderMessage(userInfoResult, myCommonConfig.getCommonAdministratorTgId(), premiumBuyId);
        return ResponseEntity.success("成功");
    }

    @Override
    public ResponseEntity<?> handleUserBuyPremiumByAuto(UserBuyPremiumVO userBuyPremiumVO,
                                           UserInfoResult userInfoResult, String lang) {
        log.info("开始进行购买会员-自动 userBuyPremiumVO={},userInfoResult={}",
                JSONUtil.toJsonStr(userBuyPremiumVO), JSONUtil.toJsonStr(userInfoResult));
        /**
         * 1、判断参数是不是非法
         */
        if (userBuyPremiumVO.getBuyType() == null || userBuyPremiumVO.getPremiumType() == null) {
            log.warn("TronManageService.handleUserBuyPremiumByAuto 非法参数,userBuyPremiumVO={},userInfoResult={}",
                    JSONUtil.toJsonStr(userBuyPremiumVO), JSONUtil.toJsonStr(userInfoResult));
            //throw new RRException(I18nUtil.getMessage("1017", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
            return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1017", lang));
        }

        /**
         * 2、判断购买的会员类型是否有效
         */
        String premium_type = sysDictDataService.getDictLabel(PREMIUM_TYPE, String.valueOf(userBuyPremiumVO.getPremiumType()));
        if (StringUtils.isBlank(premium_type)) {
            log.warn("TronManageService.handleUserBuyPremiumByAuto 购买的会员类型无效,userBuyPremiumVO={},userInfoResult={}",
                    JSONUtil.toJsonStr(userBuyPremiumVO), JSONUtil.toJsonStr(userInfoResult));
            //throw new RRException(I18nUtil.getMessage("10007", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
            return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1017", lang));
        }

        /**
         * 3、判断用户是为自己购买还是为他人购买 0是为自己购买，1是为他人购买
         */
        if (userBuyPremiumVO.getBuyType() == 0) {
            userBuyPremiumVO.setUserId(userInfoResult.getUserTGID());
            userBuyPremiumVO.setUserName(userInfoResult.getUserName());
        }

        /**
         * 4、根据TGID获取为购买的用户信息
         */
        TgBotUserResult tgBotUserResult = null;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("chatId", userBuyPremiumVO.getUserId());
        try {
            tgBotUserResult = getBotChat(userInfoResult.getUserTGID(), userInfoResult.getFirstName(), userInfoResult.getUserName(),
                    "getBotChat", paramMap,lang);
        } catch (Exception e) {
            throw new RRException("TronManageService.handleUserBuyPremiumByAuto 获取用户TG相关信息失败", ResultCodeEnum.TG_HTTP_ERROR.code);
        }

        if (tgBotUserResult == null) {
            log.warn("TronManageService.handleUserBuyPremiumByAuto 购买的用户无效,userBuyPremiumVO={},userInfoResult={}",
                    JSONUtil.toJsonStr(userBuyPremiumVO), JSONUtil.toJsonStr(userInfoResult));
            //throw new RRException(I18nUtil.getMessage("10006", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
            return ResponseEntity.failure(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1017", lang));
        }

        /**
         * 5、获取购买会员的价格
         */
        BigDecimal premium_price = null;
        switch (premium_type) {
            case PREMIUM_TYPE_0:
                TAntwalletConfig premium_type_3_month_price = tAntwalletConfigService.getById(PREMIUM_TYPE_3_MONTH_PRICE);
                premium_price = new BigDecimal(premium_type_3_month_price.getPValue());
                break;
            case PREMIUM_TYPE_1:
                TAntwalletConfig premium_type_6_month_price = tAntwalletConfigService.getById(PREMIUM_TYPE_6_MONTH_PRICE);
                premium_price = new BigDecimal(premium_type_6_month_price.getPValue());
                break;
            case PREMIUM_TYPE_2:
                TAntwalletConfig premium_type_1_year_price = tAntwalletConfigService.getById(PREMIUM_TYPE_1_YEAR_PRICE);
                premium_price = new BigDecimal(premium_type_1_year_price.getPValue());
                break;
            default:
                TAntwalletConfig premium_type_3_month_price_ = tAntwalletConfigService.getById(PREMIUM_TYPE_3_MONTH_PRICE);
                premium_price = new BigDecimal(premium_type_3_month_price_.getPValue());
                break;
        }

        /**
         * 6、判断账户中是否有足够的金额为自己购买会员或者其他用户购买会员
         */
        Integer accountType = Integer.parseInt(sysDictDataService.getDictValue(ACCOUNT_TYPE, USDT));
        TAccount tAccount = tAccountService.getAccountByUserIdAndAccountType(userInfoResult.getUserTGID(), accountType);
        if (tAccount == null) {
            log.warn("TronManageService.handleUserBuyPremiumByAuto userId={},accountType={} 没有查到用户的账户信息", userInfoResult.getUserTGID(), accountType);
            //throw new RRException(I18nUtil.getMessage("4004", lang), ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code);
            return ResponseEntity.failure(ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code, I18nUtil.getMessage("1018", lang));
        }
        BigDecimal amount = new BigDecimal(tAccount.getAmount());
        if (amount.compareTo(premium_price) < 0) {
            log.warn("TronManageService.handleUserBuyPremiumByAuto userId={},accountType={},amount={},money={} 没有足够金额进行购买会员",
                    userInfoResult.getUserTGID(), accountType, amount, premium_price);
            //throw new RRException(I18nUtil.getMessage("10002", lang), ResultCodeEnum.PREMIUM_BUY_NOT_ENOUGH.code);
            return ResponseEntity.failure(ResultCodeEnum.MONEY_NOT_ENOUGH.code, I18nUtil.getMessage("1020", lang));
        }

        /**
         * 7、开始会员购买
         */
        String premiumBuyId = "premium_buy_".concat(UidGeneratorUtil.genId());//根据雪花算法获取提现id
        tUserBuyPremiumService.saveTUserBuyPremium(premiumBuyId, userInfoResult.getUserTGID(), userBuyPremiumVO.getUserId(),
                userBuyPremiumVO.getUserName(), userBuyPremiumVO.getPremiumType(), premium_price.toString(), accountType);

        /**
         * 7、购买会员账户扣除对应的金额
         */
        BigDecimal amount_new = amount.subtract(premium_price);
        log.info("TronManageService.handleUserBuyPremiumByAuto 开始扣除购买会员账户对应的金额,付款账户id={},扣除金额={},账户金额={}", tAccount.getAccountId(), premium_price, amount);
        tAccountService.deductAccountMoney(tAccount.getId(), amount, amount_new);
        log.info("TronManageService.handleUserBuyPremiumByAuto 扣除购买会员账户余额成功,扣除金额={},账户金额={}", premium_price, amount_new);

        /**
         * 8、异步保存购买会员交易记录
         */
        Integer tradeType = Integer.parseInt(sysDictDataService.getDictValue(TRADE_TYPE, TRADE_TYPE_BUY_PREMIUM));
        String pay_trade_money = "-".concat(premium_price.toString());
        TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
        tAccountTradeService
                .saveAccountTrade(tAccount.getAccountId(), tUser.getUserId(), premiumBuyId, pay_trade_money,
                        tradeType, amount_new.toString(), accountType,null );

        log.info("TronManageService.handleUserBuyPremiumByAuto 结束购买会员 userBuyPremiumVO={},userInfoResult={},premiumBuyId={}",
                JSONUtil.toJsonStr(userBuyPremiumVO), JSONUtil.toJsonStr(userInfoResult), premiumBuyId);
        return ResponseEntity.success("成功");
    }

    /**
     * 对区块链上的资金进行归集
     */
    @Override
    public void handleCollectTronAmount() {
        log.info("TronManageService.handleCollectTronAmount 开始进行资金归集");
        /**
         * 1、获取要归集的钱包地址
         */
        List<TWalletPool> tWalletPoolList = tWalletPoolService.getTWalletPoolList();
        String taskId = "collect_task_id_".concat(UidGeneratorUtil.genId());//根据雪花算法获取归集批次id
        String collectTime = TimeUtil.sdf3.get().format(new Date());
        for (TWalletPool tWalletPool : tWalletPoolList) {
            String fromAddress = tWalletPool.getBase58CheckAddress();
            String privateKey = tWalletPool.getPrivateKey();
            /**
             * 2、归集TRX
             */
            doCollectTronAmountTRX(fromAddress, privateKey, myCommonConfig.getToAddress(), taskId, collectTime);
            /**
             * 3、归集USDT
             */
            doCollectTronAmountUSDT(fromAddress, privateKey, myCommonConfig.getToAddress(), taskId, collectTime);
        }
        log.info("TronManageService.handleCollectTronAmount 结束资金归集");
    }

    /**
     * 刷新转账交易的状态
     */
    @Override
    public void handleRefreshCollectTronStatus(String taskId, String collectTime, String lang) {
        long now = System.currentTimeMillis();
        if (StringUtils.isBlank(taskId) || StringUtils.isBlank(collectTime)) {
            log.warn("TronManageService.handleRefreshCollectTronStatus 非法参数,taskId={},collectTime={}", taskId, collectTime);
            throw new RRException(I18nUtil.getMessage("1017", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
        }
        try {
            /**
             * 1分钟后才能进行刷新操作
             */
            long collectTime_millis_10s = TimeUtil.sdf3.get().parse(collectTime).getTime() + 60 * 1000;
            if (now < collectTime_millis_10s) {
                log.warn("TronManageService.handleRefreshCollectTronStatus 不允许1分钟内刷新归集状态 taskId={},collectTime={} ",
                        taskId, collectTime);
                throw new RRException(I18nUtil.getMessage("5011", lang), ResultCodeEnum.TRUNSFER_NOT_REFRESH.code);
            }

            /**
             * 2、进行刷新
             */
            List<TTronCollectRecord> tTronCollectRecordList = tTronCollectRecordService.getTTronCollectRecordListByTaskId(taskId);
            for (TTronCollectRecord tTronCollectRecord : tTronCollectRecordList) {
                /**
                 * 3当归集状态是100时，说明已经构建交易成功，需要进行状态查询，不是100时则不查询
                 */
                if (!tTronCollectRecord.getStatus().equals(COLLECT_TRON_STATUS_100)) {
                    continue;
                }

                TWalletPool tWalletPool = tWalletPoolService.getTWalletPoolByAddress(tTronCollectRecord.getFromAddress());
                if(tWalletPool == null ){
                   continue;
                }

                String collect_online_status = tronUtils.getTransactionStatusById(tWalletPool.getPrivateKey(),tTronCollectRecord.getCollectTxid());
                if (collect_online_status.contains("SUCCESS")) {
                    tTronCollectRecordService.modifyTTronCollectRecord(tTronCollectRecord.getId(), COLLECT_TRON_STATUS_200, tTronCollectRecord.getTronType() + "归集成功", null);
                } else {
                    tTronCollectRecordService.modifyTTronCollectRecord(tTronCollectRecord.getId(), COLLECT_TRON_STATUS_400, collect_online_status, null);
                }
            }
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rRException = (RRException) e;
                throw rRException;
            } else {
                log.error(String.format("TronManageService.handleRefreshCollectTronStatus 刷新交易状态,taskId={%s},collectTime={%s}具体失败信息:", taskId, collectTime), e);
                throw new RRException(String.format("TronManageService.handleRefreshCollectTronStatus 刷新交易状态,taskId={%s},collectTime={%s}", taskId, collectTime), ResultCodeEnum.SYSTEM_ERROR_500.code);
            }
        }

    }

    @Override
    public List<CollectTronResult> getCollectTronResultList() {
        return tTronCollectRecordService.getTTronCollectRecordGroupByTaskId();
    }

    @Override
    public List<TTronCollectRecord> getCollectTronRecordDetails(String taskId, HttpServletRequest httpRequest) {
        //获取用户的语言
        String lang = httpRequest.getHeader("lang");
        if (StringUtils.isEmpty(lang)) {
            lang = "en-US";
        }
        if (StringUtils.isBlank(taskId)) {
            log.warn("TronManageService.getCollectTronRecordDetails 非法参数,taskId={}", taskId);
            throw new RRException(I18nUtil.getMessage("1017", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
        }
        return tTronCollectRecordService.getTTronCollectRecordListByTaskId(taskId);
    }


    @Override
    public void handleWithdrawAuto(Long id,String toAddress,String blockchainType, String withdrawType, String withdrawMoney, HttpServletRequest httpRequest) {
        log.info("TronManageService.handleWithdrawAuto 开始执行自动提币任务");
        if (id == null ||StringUtils.isBlank(toAddress) || StringUtils.isBlank(blockchainType) || StringUtils.isBlank(withdrawType) || StringUtils.isBlank(withdrawMoney)) {
            log.error("TronManageService.handleWithdrawAuto 非法参数,id={},toAddress={},blockchainType={},withdrawType={},withdrawMoney={}",id, toAddress, blockchainType, withdrawType, withdrawMoney);
            return;
        }
        try {
            blockchainType = sysDictDataService.getDictLabel(BLCOKCHAIN_TYPE, blockchainType);

            BigDecimal withdrawMoney_ = new BigDecimal(withdrawMoney);
            BigDecimal withdrawMoney_1000000 = new BigDecimal("1000000");
            BigDecimal withdrawMoney_new = withdrawMoney_.multiply(withdrawMoney_1000000);

            if (blockchainType.equals(BLCOKCHAIN_TYPE_TRON)) {//tron波场链上usdt提现
                /**
                 * 获取区块链地址上的USDT余额
                 */
                BigDecimal usdtBalanceOf = tronUtils.getAccountUSDTBalance(myCommonConfig.getWithdrawHexPrivateKey(), myCommonConfig.getWithdrawAddress());

                /**
                 * 判断提现地址上是否有足够余额提现
                 */
                if (withdrawMoney_new.compareTo(usdtBalanceOf) > 0) {
                    log.error(
                            "TronManageService.handleWithdrawAuto USDT自动提现失败,提现地址余额不足,toAddress={},blockchainType={},withdrawType={},withdrawMoney={},usdtBalanceOf={}",
                            toAddress, blockchainType, withdrawType, withdrawMoney, usdtBalanceOf.toString());
                    return;
                }
                /**
                 * 开始提现
                 */
                WithdrawUSDTData withdrawUSDTData = tronUtils.trunsferUSDT(myCommonConfig.getWithdrawHexPrivateKey(),
                        myCommonConfig.getWithdrawAddress(), toAddress,
                        withdrawMoney_new.toBigInteger());
                log.info("TronManageService.handleWithdrawAuto 提现使用的能量为：{}", withdrawUSDTData.getEnergy());
                //获取到此次购买tron链上时的手续费
                Integer conversion_price = new Integer(tAntwalletConfigService.selectTAntwalletbotConfigByPKey(PRICE).getPValue());
                Integer i = (Integer.parseInt(withdrawUSDTData.getEnergy()) / conversion_price) + 1;
                log.info("TronManageService.handleWithdrawAuto 购买的能量的笔数是：{}", i);
                /**
                 * 保存提现的txid
                 */
                tUserWithdrawalService.updateTUserWithdrawal(id, withdrawUSDTData.getTxid(), 3, String.valueOf(i));
            } else if (blockchainType.equals(BLCOKCHAIN_TYPE_TON)) { //ton链上usdt提现
                /**
                 * 开始ton链usdt提现
                 */
                String transfer_command = "cd " + myCommonConfig.getTonConfigPath() + " && " +
                        " python3 antwalletbot_tonutils.py -t transfer -addr " + toAddress + " -amt " + withdrawMoney;
                Map<String, Object> transfer_result_map = CmdUtil.procCmd(transfer_command);
                if (!"200".equals(transfer_result_map.get("code"))) {
                    log.error("TronManageService.handleWithdrawAuto USDT自动提现失败,toAddress={},blockchainType={},withdrawType={},withdrawMoney={},response={}",
                            toAddress, blockchainType, withdrawType, withdrawMoney, JSONUtil.toJsonStr(transfer_result_map));
                    return;
                }
                String transfer_result = transfer_result_map.get("htmlWebPage").toString();
                if (StringUtils.isBlank(transfer_result) || transfer_result.contains("Error")) {
                    log.error("TronManageService.handleWithdrawAuto USDT自动提现失败,toAddress={},blockchainType={},withdrawType={},withdrawMoney={},response={}",
                            toAddress, blockchainType, withdrawType, withdrawMoney, JSONUtil.toJsonStr(transfer_result_map));
                    return;
                }

                String txid = transfer_result.replace("Success:", "");
                /**
                 * 保存提现的txid
                 */
                tUserWithdrawalService.updateTUserWithdrawal(id, txid, 3, null);
            }

        } catch (Exception e) {
            log.error("handleWithdrawAuto-自动提现失败,fromAddress={},toAddress={},blockchainType={},withdrawType={},withdrawMoney={} 具体失败信息:"
                    , myCommonConfig.getWithdrawAddress(), toAddress, blockchainType, withdrawType, withdrawMoney, e);
        }

    }

    @Override
    public void refreshWithdrawStatus(Long id, HttpServletRequest httpRequest) {
        //获取用户的语言
        String lang = httpRequest.getHeader("lang");
        if (StringUtils.isEmpty(lang)) {
            lang = "en-US";
        }
        if (id == null) {
            log.warn("TronManageService.refreshWithdrawStatus 非法参数,id={}",id);
            throw new RRException(I18nUtil.getMessage("1017", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
        }

        try {
            TUserWithdrawal tUserWithdrawal = tUserWithdrawalService.getById(id);

            if (tUserWithdrawal == null) {
                log.warn("TronManageService.refreshWithdrawStatus 不存在此提现记录,id={}", id);
                throw new RRException(I18nUtil.getMessage("9002", lang),
                    ResultCodeEnum.WITHDRAWAL_IS_NOT_EXSIT.code);
            }

            if(StringUtils.isBlank(tUserWithdrawal.getWithdrawalTxid())){
                log.warn("TronManageService.refreshWithdrawStatus 刷新提现记录失败,tid不存在,tUserWithdrawal={}", JSONUtil.toJsonStr(tUserWithdrawal));
                throw new RRException(I18nUtil.getMessage("9002", lang),
                        ResultCodeEnum.WITHDRAWAL_AUTO_ERROR.code);
            }

            if (tUserWithdrawal.getStatus() != check_withdrawal_status_1) {
                return;
            }

            String blockchainType = sysDictDataService
                .getDictLabel(BLCOKCHAIN_TYPE, tUserWithdrawal.getBlockchainType().toString());

           if(blockchainType.equals(BLCOKCHAIN_TYPE_TRON)) {//tron波场链上usdt提现
                //Thread.sleep(5000);
                String status = tronUtils.getTransactionStatusById(myCommonConfig.getWithdrawHexPrivateKey(),tUserWithdrawal.getWithdrawalTxid());
                if (!status.contains("SUCCESS")) {
                    tUserWithdrawalService.updateTUserWithdrawal(id, null, check_withdrawal_status_4,null );
                    return;
                }
               tUserWithdrawalService.updateTUserWithdrawal(id, null, check_withdrawal_status_3,null );
           }


            if(blockchainType.equals(BLCOKCHAIN_TYPE_TON)) {
                //Thread.sleep(5000);

                 /**
                   * 查询ton链usdt提现状态
                   */

                    String transfer_status_command = "cd " + myCommonConfig.getTonConfigPath() + " && " +
                        " python3 antwalletbot_tonutils.py -g gettxstatus -tid "+ tUserWithdrawal.getWithdrawalTxid();
                    Map<String, Object> transfer_status_result_map  = CmdUtil.procCmd(transfer_status_command);
                    if (!"200".equals(transfer_status_result_map.get("code"))) {
                        log.warn(
                            "TronManageService.refreshWithdrawStatus 刷新自动提现状态失败,tUserWithdrawal={},response={}",
                             JSONUtil.toJsonStr(tUserWithdrawal),JSONUtil.toJsonStr(transfer_status_result_map));
                        throw new RRException(I18nUtil.getMessage("9002", lang), ResultCodeEnum.WITHDRAWAL_AUTO_ERROR.code);
                    }
                    String transfer_status_result = transfer_status_result_map.get("htmlWebPage").toString();
                    if(transfer_status_result.contains("Error")){
                        log.warn(
                            "TronManageService.TronManageService.refreshWithdrawStatus 刷新自动提现状态失败,tUserWithdrawal={},response={}",
                            JSONUtil.toJsonStr(tUserWithdrawal),JSONUtil.toJsonStr(transfer_status_result_map));
                        throw new RRException(I18nUtil.getMessage("9002", lang), ResultCodeEnum.WITHDRAWAL_AUTO_ERROR.code);
                    }

                    String status = transfer_status_result.replace("Success:","");


                    /**
                     * 查询一笔ton链上得交易
                     */

                    String withdrawalBlockchainFee = "";

                    String transfer_fee_command = "cd " + myCommonConfig.getTonConfigPath() + " && " +
                            " python3 antwalletbot_tonutils.py -fee gettxfee -tid "+ tUserWithdrawal.getWithdrawalTxid();
                    Map<String, Object> transfer_fee_result_map  = CmdUtil.procCmd(transfer_fee_command);
                    if (!"200".equals(transfer_fee_result_map.get("code"))) {
                        log.warn(
                                "TronManageService.refreshWithdrawStatus 查询自动提现ton链上得手续费失败,tUserWithdrawal={},response={}",
                                JSONUtil.toJsonStr(tUserWithdrawal),JSONUtil.toJsonStr(transfer_fee_result_map));
                        withdrawalBlockchainFee = "";
                    } else {
                        String transfer_fee_result = transfer_fee_result_map.get("htmlWebPage").toString();
                        if (transfer_fee_result.contains("Error")) {
                            log.warn(
                                    "TronManageService.TronManageService.refreshWithdrawStatus 查询自动提现ton链上得手续费失败,tUserWithdrawal={},response={}",
                                    JSONUtil.toJsonStr(tUserWithdrawal), JSONUtil.toJsonStr(transfer_fee_result_map));
                            withdrawalBlockchainFee = "";
                        }else{
                            withdrawalBlockchainFee = transfer_fee_result.replace("Success:", "");
                        }
                    }

                    if (!status.contains("True")) {
                        tUserWithdrawalService.updateTUserWithdrawal(id,null,check_withdrawal_status_4,withdrawalBlockchainFee );
                        return;
                    }
                    tUserWithdrawalService.updateTUserWithdrawal(id,null,check_withdrawal_status_3,withdrawalBlockchainFee );
             }


        }catch (Exception e) {
            if (e instanceof RRException) {
                RRException rRException = (RRException) e;
                throw rRException;
            } else {
                log.error(String.format("TronManageService.refreshWithdrawStatus 刷新自动提现状态失败,id={%s},具体失败信息",  id), e);
                throw new RRException(String.format("TronManageService.refreshWithdrawStatus 刷新自动提现状态失败,id={%s}", id), ResultCodeEnum.SYSTEM_ERROR_500.code);
            }
        }

    }

    @Override
    public TronTransResult getTronTransAccount(String tronTransId, UserInfoResult userInfoResult, String lang) {

        log.info("开始查询收款详情,tronTransId={},receiveTGId={}", tronTransId, userInfoResult.getUserTGID());
        /**
         * 1、判断传过来的参数是否符合
         */
        if (StringUtils.isBlank(tronTransId)) {
            log.warn("tronManageService.getTronTransAccount 非法参数,tronTransId={},receiveTGId={}", tronTransId, userInfoResult.getUserTGID());
            throw new RRException(I18nUtil.getMessage("1000", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
        }


        /**
         * 2、判断群组红包是否存在
         */
        String repacketCache = redisOperate.getRedis(CommonConstant.SEND_GROUP_REDPACKET + tronTransId);
        TTronTrans tTronTrans = tTronTransService.getTTronTrans(tronTransId);;
        /*if (StringUtils.isBlank(repacketCache)) {

            if (null == tTronTrans) {
                log.warn("tronManageService.getTronTransAccount 抢的红包不存在,tronTransId={}", tronTransId);
                throw new RRException(I18nUtil.getMessage("4007", lang), ResultCodeEnum.SINGLE_REDPACKET_IS_NOT_EXIST.code);
            }
        } else {
            tTronTrans = JSONUtil.toBean(repacketCache, TTronTrans.class);
        }*/
        long now = System.currentTimeMillis() / 1000;

        if (tTronTrans.getPaymentExpiryTime()!=null){
            Integer tron_trans_status_5 = Integer.parseInt(sysDictDataService.getDictValue(TRON_TRANS_STATUS, TRON_TRANS_STATUS_5));
            long expire_time = tTronTrans.getPaymentExpiryTime().atZone(ZoneId.of("Asia/Shanghai")).toEpochSecond(); // 将LocalDateTime转换为Timestamp并获取其时间戳（单位：毫秒）
            if (tTronTrans.getStatus().equals(tron_trans_status_5) || expire_time <= now) {
                tTronTransService.modifyTTronTransStatus(tTronTrans.getId(), tron_trans_status_5);
                log.warn("tronManageService.getTronTransAccount 收款已过期,过期,tronTransId={},receiveTGId={}", tronTransId, userInfoResult.getUserTGID());
                //throw new RRException(I18nUtil.getMessage("609", lang), ResultCodeEnum.TTRONTRANS_IS_EXPIRE.code);
                tTronTrans.setStatus(tron_trans_status_5);
            }
        }

        /**
         * 4、判断领取红包的用户是否存在
         */
        TUser receiveTUser = userMapLocalCache.get(userInfoResult.getUserTGID());
        if (null == receiveTUser) {
            receiveTUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
            if (receiveTUser == null) {
                log.warn("tronManageService.getTronTransAccount 抢红包的用户不存在,tronTransId={},receiveTGId={}", tronTransId, userInfoResult.getUserTGID());
                throw new RRException(I18nUtil.getMessage("4002", lang), ResultCodeEnum.USER_IS_NOT_EXIST.code);
            }
            userMapLocalCache.put(userInfoResult.getUserTGID(), receiveTUser);
        }

        /**
         * 5、返回前端数据
         */
        TronTransResult result = new TronTransResult();
        result.setTronTransId(tronTransId);
        TUser sendTUser = tUserService.getTUserByTGId(tTronTrans.getSendUserId());
        result.setSendUserName("@" + sendTUser.getName());
        result.setSendUserNick("@" + sendTUser.getNick());
        result.setSendUserAvatar(sendTUser.getAvatar());
        result.setComment(tTronTrans.getComment());
        result.setAccountType(tTronTrans.getAccountType());
        result.setTronTransNum(tTronTrans.getTransNum());
        String accountType = sysDictDataService.getDictLabel(ACCOUNT_TYPE, tTronTrans.getAccountType().toString());
        BigDecimal money = new BigDecimal(tTronTrans.getMoney());
        result.setMoney(money.toString());
        result.setTronTransType(tTronTrans.getTransType());

        /**
         * 返回领取条件信息
         */
        if (StringUtils.isNotBlank(tTronTrans.getChannelConditions()) || StringUtils.isNotBlank(tTronTrans.getGroupsConditions())) {
            if (StringUtils.isNotBlank(tTronTrans.getChannelConditions())){
                result.setChannelConditions(tTronTrans.getChannelConditions());
            }
            if (StringUtils.isNotBlank(tTronTrans.getGroupsConditions())){
                result.setGroupsConditions(tTronTrans.getGroupsConditions());
            }
        }
        if (StringUtils.isNotBlank(tTronTrans.getSubscriptionDesc())) {
            result.setSubscriptionDesc(tTronTrans.getSubscriptionDesc());
        }
        result.setSubscriptionHours(tTronTrans.getSubscriptionHours());
        result.setStatus(tTronTrans.getStatus());
        result.setPaymentCount(tTronTrans.getPaymentCount());
        result.setStatus(tTronTrans.getStatus());
        result.setSubscriptionExpiryTime(tTronTrans.getSubscriptionExpiryTime());
        result.setPaymentExpiryTime(tTronTrans.getPaymentExpiryTime());
        result.setCustomerServiceLink(tTronTrans.getCustomerServiceLink());
        result.setSharingRatio(tTronTrans.getSharingRatio());
        log.info("结束查询收款详情,tronTransId={},receiveTGId={}", tronTransId, userInfoResult.getUserTGID());
        return result;
    }

    @Override
    public R handleUserPayment(String tronTransId, String receiveTGId, String lang, String shareUserId) {
        log.info("tronManageService.handleUserPayment 开始用户付款,sendTGId={},receiveTGId={}", tronTransId, receiveTGId);
        long now = System.currentTimeMillis() / 1000;
        String lockKey = "tg_user_payment_lock_" + tronTransId;
        RLock lock = redissonClient.getLock(lockKey);
        TransactionStatus transactionStatus = null; // 用于保存事务状态
        try {
            //并发抢红包加锁 操作很类似Java的ReentrantLock机制
            lock.lock();
            // 开启事务
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED); // 设置事务传播行为
            transactionStatus = transactionManager.getTransaction(def); // 开始事务
            /*
             * 1、判断传过来的参数是否符合
             */
            if (StringUtils.isBlank(tronTransId) || StringUtils.isBlank(receiveTGId)) {
                log.warn("tronManageService.handleUserPayment 用户付款失败,非法参数,tronTransId={},receiveTGId={}", tronTransId, receiveTGId);
                throw new RRException(I18nUtil.getMessage("4002", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
            }

            /*
             * 2、判断发起收款的记录是否存在
             */
            TTronTrans tTronTrans = tTronTransService.getTTronTrans(tronTransId);
            if (tTronTrans == null) {
                log.warn("tronManageService.handleUserPayment 发起收款的记录不存在,tronTransId={},receiveTGId={}", tronTransId, receiveTGId);
                throw new RRException(I18nUtil.getMessage("607", lang), ResultCodeEnum.TTRONTRANS_IS_NOT_EXIST.code);
            }

            /*
             * 3、判断群组红包是否过期,修改群组红包状态为已过期 当他是空，则代码收款是无限期
             */
            if (tTronTrans.getPaymentExpiryTime()!=null){
                Integer tron_trans_status_5 = Integer.parseInt(sysDictDataService.getDictValue(TRON_TRANS_STATUS, TRON_TRANS_STATUS_5));
                long expire_time = tTronTrans.getPaymentExpiryTime().atZone(ZoneId.of("Asia/Shanghai")).toEpochSecond(); // 将LocalDateTime转换为Timestamp并获取其时间戳（单位：毫秒）
                if (tTronTrans.getStatus().equals(tron_trans_status_5) || expire_time <= now) {
                    tTronTransService.modifyTTronTransStatus(tTronTrans.getId(), tron_trans_status_5);
                    log.warn("tronManageService.handleUserPayment 收款已过期,过期,tronTransId={},receiveTGId={}", tronTransId, receiveTGId);
                    //throw new RRException(I18nUtil.getMessage("609", lang), ResultCodeEnum.TTRONTRANS_IS_EXPIRE.code);
                    return R.error(ResultCodeEnum.TTRONTRANS_IS_EXPIRE.code, I18nUtil.getMessage("4021",lang));
                }
            }


            /*
             * 4、判断收款记录是否付款完，首先判断 redis中的收款库存是不是已经抢完了，再然后判断数据库中的库存
             */
            Integer redpacket_leave_num = tTronTrans.getTransNum() - tTronTrans.getPaymentCount();
            String redpacket_leave_redis = redisOperate.getronTransToRedis(tronTransId);
            if (StringUtils.isBlank(redpacket_leave_redis) || Integer.parseInt(redpacket_leave_redis) <= 0 || redpacket_leave_num <= 0) {
                log.warn("tronManageService.handleUserPayment 付款记录已经付款结束,redpacketId={},receiveTGId={}", tronTransId, receiveTGId);
                //throw new RRException(I18nUtil.getMessage("4011", lang), ResultCodeEnum.REDPACKET_RECEIVE_FINISHED.code);
                return R.error(ResultCodeEnum.REDPACKET_RECEIVE_FINISHED.code, I18nUtil.getMessage("5001",lang));
            }

            /*
             * 6、判断领取红包的用户是否存在
             */
            TUser sendTUser = userMapLocalCache.get(tTronTrans.getSendUserId());
            if (null == sendTUser) {
                sendTUser = tUserService.getTUserByTGId(tTronTrans.getSendUserId());
            }

            TUser receiveTUser = userMapLocalCache.get(receiveTGId);
            if (null == receiveTUser) {
                receiveTUser = tUserService.getTUserByTGId(receiveTGId);
                if (receiveTUser == null) {
                    log.warn("tronManageService.handleUserPayment 付款的用户不存在,tronTransId={},receiveTGId={}", tronTransId, receiveTGId);
                    throw new RRException(I18nUtil.getMessage("4002", lang), ResultCodeEnum.USER_IS_NOT_EXIST.code);
                }
            }

            ZoneId zoneId = ZoneId.of("Asia/Shanghai");
            /**
             * 8、判断用户是否付过款
             */
            long duration1 = 0L;
            Integer payment_status_5 = Integer.parseInt(sysDictDataService.getDictValue(PAYMENT_STATUS, PAYMENT_STATUS_5));
            TPaymentRecords tPaymentRecords = tPaymentRecordsService.getLatestPaymentRecord(tronTransId, receiveTUser.getUserId());
            if (tPaymentRecords != null && tPaymentRecords.getSubscriptionExpiryTime()!=null) {
                log.warn("tronManageService.handleUserPayment 已付款,tronTransId={},receiveTGId={},receiveUserId={}", tronTransId, receiveTGId, receiveTUser.getUserId());
                //如果未过期则修改他的状态、以及他的时间
                if ( tPaymentRecords.getSubscriptionExpiryTime().atZone(zoneId).toEpochSecond() >= now){
                    //修改状态以及订阅收款时间进行续费
                    tPaymentRecordsService.modifyTPaymentRecordsStatusAndSubscriptionExpiryTime(tPaymentRecords.getId(), payment_status_5);
                    LocalDateTime subscriptionExpiryTime1 = tPaymentRecords.getSubscriptionExpiryTime();
                    LocalDateTime now1 = LocalDateTime.now();
                    duration1 = Duration.between(now1, subscriptionExpiryTime1).toMinutes();
                }
            }

            /**
             * 9 、判断领取红包的用户是否存在账户
             */
            TAccount tAccount = tAccountService.getAccountByUserIdAndAccountType(receiveTGId, tTronTrans.getAccountType());
            if (tAccount == null) {
                log.warn("tronManageService.handleUserPayment 没有查到领取红包的账户信息,收款用户[{}],付款用户[{}]," + "具体收款信息:{}", sendTUser.getName(), receiveTUser.getName(), JSONUtil.toJsonStr(tTronTrans));
                throw new RRException(I18nUtil.getMessage("4004", lang), ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code);
            }

            /**
             * 11、获取领取的金额和账户金额
             */
            BigDecimal money;
            try {
                money = new BigDecimal(tTronTrans.getMoney());
            } catch (Exception e) {
                log.error(String.format("tronManageService.handleUserPayment  金额的格式有问题,收款用户[%s],付款用户[%s]," + "具体红包信息:%s", sendTUser.getName(), receiveTUser.getName(), JSONUtil.toJsonStr(tTronTrans)), e);
                throw new RRException(I18nUtil.getMessage("4003",lang), ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
            }

            /**
             * 14、扣除redis红包数量
             */
            redisOperate.deductTronTransToRedis(tronTransId);

            /**
             * 15、增加数据库中的付款的次数
             */
            tTronTransService.addTronTransPaymentCount(tTronTrans,1);

            String chatName="";
            if (StringUtils.isNotBlank(tTronTrans.getChannelConditions())){
                chatName=tTronTrans.getChannelConditions();
            }
            if (StringUtils.isNotBlank(tTronTrans.getGroupsConditions())){
                chatName=tTronTrans.getGroupsConditions();
            }
            /**
             * 16、增加抢红包记录 ,该记录状态已设置为 存入余额
             */
            long totalDuration=0;
            if (tTronTrans.getSubscriptionExpiryTime()!=null){
                LocalDateTime subscriptionExpiryTime = tTronTrans.getSubscriptionExpiryTime();
                LocalDateTime createTime = tTronTrans.getCreateTime();
                long duration = Duration.between(createTime, subscriptionExpiryTime).toMinutes();
                // 将两个持续时间相加
                totalDuration = duration + duration1;
            }
            Integer payment_status_0 = Integer.parseInt(sysDictDataService.getDictValue(PAYMENT_STATUS, PAYMENT_STATUS_0));
            Integer payment_status_4 = Integer.parseInt(sysDictDataService.getDictValue(PAYMENT_STATUS, PAYMENT_STATUS_4));
            // 普通收款直接把金额传入账号中
            String paymentId = "payment_".concat(UidGeneratorUtil.genId());//根据雪花算法获取收款id
            if (StringUtils.isBlank(tTronTrans.getChannelConditions()) && StringUtils.isBlank(tTronTrans.getGroupsConditions())){
                tPaymentRecordsService.saveTPaymentRecords(tronTransId, paymentId, tTronTrans.getSendUserId(), receiveTUser.getUserId(),
                        String.valueOf(money), tTronTrans.getAccountType(), totalDuration, chatName, payment_status_4, tTronTrans.getSharingRatio(),shareUserId);
            }else {
                tPaymentRecordsService.saveTPaymentRecords(tronTransId, paymentId,tTronTrans.getSendUserId(), receiveTUser.getUserId(),
                        String.valueOf(money), tTronTrans.getAccountType(),totalDuration,chatName, payment_status_0, tTronTrans.getSharingRatio(),shareUserId);
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(tAccount.getAmount());
            } catch (Exception e) {
                log.error(String.format("tronManageService.handleUserPayment 账户金额的格式有问题,发送红包用户[{}],接收红包用户[%s],账户金额[%s]" + "具体红包信息:%s", sendTUser.getName(), receiveTUser.getName(), tAccount.getAmount(), JSONUtil.toJsonStr(tTronTrans)), e);
                throw new RRException(I18nUtil.getMessage("4003",lang), ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(money) < 0) {
                log.warn("RedPacketManageService.handleUserPayment 账户余额不足,发送红包用户[{}],账户余额[{}]" + "具体红包信息:{}", sendTUser.getName(), amount, JSONUtil.toJsonStr(tTronTrans));
                throw new RRException(I18nUtil.getMessage("1011", lang), ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code);
            }
            /**
             * 扣除账户对应的金额
             */
            log.info("tronManageService.handleUserPayment 开始扣除账户对应的金额,扣除金额={},账户金额={}", money, amount);
            tAccountService.deductAccountMoney(tAccount.getId(), amount, money);
            log.info("tronManageService.handleUserPayment 扣除账户余额成功,扣除金额={},账户金额={}", money, amount.subtract(money));
            /**
             * 异步保存交易记录
             */
            Integer tradeType = Integer.parseInt(sysDictDataService.getDictValue(TRADE_TYPE, TRADE_TYPE_PAYMENT));
            String trade_money = "-".concat(money.toString());
            BigDecimal amount_new = amount.subtract(money);
            tAccountTradeService.saveAccountTrade(tAccount.getAccountId(), receiveTUser.getUserId(), paymentId, trade_money,
                    tradeType, amount_new.toString(), tTronTrans.getAccountType(), null);

            /**
             * 16.1 普通收款支付成功后，需要判断是否开启了分享的比例的操作 TODO
             */
            BigDecimal shareMoney = BigDecimal.ZERO;
            if (StringUtils.isNotBlank(tTronTrans.getSharingRatio()) && !shareUserId.equals(receiveTUser.getUserId())) {
                log.info("RedPacketManageService.handleUserPayment 开始处理分享的比例,比例值是:{}", tTronTrans.getSharingRatio());
                /**
                 * 2、获取分享人应该得到的金额
                 */
                shareMoney = money.multiply(new BigDecimal(tTronTrans.getSharingRatio()));
                money = money.subtract(shareMoney); //扣除分享比例的金额
                handleSharingRatio(shareUserId, shareMoney, tTronTrans.getTronTransId(), lang);
                //异步统计分享的人数
                tPaymentShareRecordsService.saveTPaymentShareRecords(tronTransId, paymentId, shareUserId, receiveTUser.getUserId());
                log.info("RedPacketManageService.handleUserPayment 分享比例处理完毕,分享人[{}],分享金额[{}]", shareUserId, shareMoney);
            }

            if (StringUtils.isBlank(tTronTrans.getChannelConditions()) && StringUtils.isBlank(tTronTrans.getGroupsConditions())){
                /**
                 * 9 、判断领取红包的用户是否存在账户
                 */
                TAccount tAccountTrans = tAccountService.getAccountByUserIdAndAccountType(tTronTrans.getSendUserId(), tTronTrans.getAccountType());
                if (tAccountTrans == null) {
                    log.warn("tronManageService.handleUserPayment 没有查到收款用户的账户信息,收款用户[{}],付款用户[{}]," + "具体收款信息:{}", sendTUser.getName(), receiveTUser.getName(), JSONUtil.toJsonStr(tTronTrans));
                    throw new RRException(I18nUtil.getMessage("4004", lang), ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code);
                }
                BigDecimal amountTrans;
                try {
                    amountTrans = new BigDecimal(tAccountTrans.getAmount());
                } catch (Exception e) {
                    log.error(String.format("tronManageService.handleUserPayment 账户金额的格式有问题,发送红包用户[{}],接收红包用户[%s],账户金额[%s]" + "具体红包信息:%s", sendTUser.getName(), receiveTUser.getName(), tAccount.getAmount(), JSONUtil.toJsonStr(tTronTrans)), e);
                    throw new RRException(I18nUtil.getMessage("4003",lang), ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
                }
                /**
                 * 扣除账户对应的金额
                 */
                log.info("RedPacketManageService.handleUserPayment 开始账户增加对应的金额,增加金额={},账户金额={}", money, amountTrans);
                tAccountService.addAccountMoney(tAccountTrans.getId(), amountTrans, money);
                log.info("RedPacketManageService.handleUserPayment 增加账户金额成功,增加金额={},账户金额={}", money, amountTrans.add(money));

                /**
                 * 异步保存交易记录
                 */
                Integer trade_type_collection = Integer.parseInt(sysDictDataService.getDictValue(TRADE_TYPE, TRADE_TYPE_COLLECTION));
                String collection_money = "+".concat(money.toString());
                BigDecimal amount_new_collection = amountTrans.add(money);
                tAccountTradeService.saveAccountTrade(tAccountTrans.getAccountId(), sendTUser.getUserId(), tronTransId, collection_money, trade_type_collection, amount_new_collection.toString(), tTronTrans.getAccountType(),null);

                //发送付款通知
                HashMap<String, Object> map = new HashMap<>();
                map.put("time",LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                map.put("money",money);
                map.put("remitter",receiveTUser.getName());
                map.put("lang",sendTUser.getLanguage());
                sendTGMessage(sendTUser.getTgId(), sendTUser.getNick(), sendTUser.getName(), "handleUserPaymentMessage",  map);
            }

            /**
             * 17、修改群组红包的状态
             * 当第一次付款修改状态为付款中，当最后一次付款则修改为已结束
             */
            Integer tron_trans_status_1 = Integer.parseInt(sysDictDataService.getDictValue(TRON_TRANS_STATUS, TRON_TRANS_STATUS_1));//收款中
            Integer tron_trans_status_4 = Integer.parseInt(sysDictDataService.getDictValue(TRON_TRANS_STATUS, TRON_TRANS_STATUS_4));//领取完
            //异步修改红包状态
            if (tTronTrans.getTransNum().equals(tTronTrans.getPaymentCount()) ) {
                tTronTransService.modifyTTronTransStatus(tTronTrans.getId(), tron_trans_status_4);
            } else {
                tTronTransService.modifyTTronTransStatus(tTronTrans.getId(), tron_trans_status_1);
            }

            /**
             * 18、领取完群组红包后删除redis的红包记录
             */
            if ((Integer.parseInt(redpacket_leave_redis) - 1) <= 0 || (redpacket_leave_num - 1) <= 0) {
                redisOperate.removeGroupRedpacketToRedis(tronTransId);
            }

            /**
             * 19、返回前端数据
             */
            TronTransResult result = new TronTransResult();
            result.setTronTransId(tronTransId);
            result.setSendUserName(sendTUser.getName());
            result.setSendUserNick(sendTUser.getNick());
            result.setComment(tTronTrans.getComment());
            result.setTronTransNum(tTronTrans.getTransNum());
            result.setTronTransType(tTronTrans.getTransType());
            result.setStatus(tTronTrans.getStatus());
            result.setMoney(money.toString());
            result.setAccountType(tTronTrans.getAccountType());
            result.setPaymentAmount(tTronTrans.getPaymentCount());

            log.info("tronManageService.handleUserPayment 用户[{}]结束付款,tronTransId={}", receiveTUser.getName(), tronTransId);

            if (StringUtils.isNotBlank(chatName)){
                HashMap<String, Object> map = new HashMap<>();
                map.put("chatName", chatName);
                map.put("lang", receiveTUser.getLanguage());
                //邀请用户的链接
                TUser finalReceiveTUser = receiveTUser;
                CompletableFuture.runAsync(()-> sendTGMessage(receiveTGId, finalReceiveTUser.getNick(), finalReceiveTUser.getName(), "handleChatInviteUser",  map));
            }

            // 提交事务
            transactionManager.commit(transactionStatus);
            return R.ok().put("result",result);
        } catch (Exception e) {
            // 回滚事务
            if (transactionStatus != null) {
                log.error(String.format("tronManageService.handleUserPayment 回滚事务,tronTransId={%s},receiveTGId={%s}，具体失败信息:", tronTransId, receiveTGId), e);
                // 回滚事务
                transactionManager.rollback(transactionStatus);
                // redis补偿机制
                redisOperate.compensateTronTransToRedis(tronTransId);
            }
            if (e instanceof RRException) {
                RRException rRException = (RRException) e;
                throw rRException;
            } else {
                log.error(String.format("tronManageService.handleUserPayment 付款失败,tronTransId={%s},receiveTGId={%s}，具体失败信息:", tronTransId, receiveTGId), e);
                throw new RRException(String.format("tronManageService.handleUserPayment " + I18nUtil.getMessage("1023", lang) + ",redpacketId={%s},receiveTGId={%s}", tronTransId, receiveTGId), ResultCodeEnum.SYSTEM_ERROR_500.code);
            }
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }


    /**
     * 处理用户比例分层的操作
     */
    @Async
    public void handleSharingRatio(String shareUserId,  BigDecimal money, String tronTransId,String lang){

        log.info("tronManageService.handleSharingRatio 分享的金额是:{}", money);
        /**
         * 1、判断分享人是否存在
         */
        TUser shareTUser = userMapLocalCache.get(shareUserId);
        if (null == shareTUser) {
            shareTUser = tUserService.getTUserByUserId(shareUserId);
            if (shareTUser == null) {
                log.warn("tronManageService.handleSharingRatio 分享的用户不存在,shareUserId={}", shareUserId);
                throw new RRException(I18nUtil.getMessage("4002", lang), ResultCodeEnum.USER_IS_NOT_EXIST.code);
            }
        }


        /**
         * 3、给分享人增加余额
         */

        TAccount tAccountTrans = tAccountService.getAccountByUserIdAndAccountType(shareTUser.getTgId(), 1);
        if (tAccountTrans == null) {
            log.warn("tronManageService.handleSharingRatio 没有查到分享用户的账户信息,分享收款用户[{}]" ,  shareTUser.getName());
            throw new RRException(I18nUtil.getMessage("4004", lang), ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code);
        }

        BigDecimal amountTrans;
        try {
            amountTrans = new BigDecimal(tAccountTrans.getAmount());
        } catch (Exception e) {
            log.error(String.format("tronManageService.handleSharingRatio 账户金额的格式有问题,分享收款用户[{}],账户金额[%s]", shareTUser.getName(),  tAccountTrans.getAmount()), e);
            throw new RRException(I18nUtil.getMessage("4003", lang), ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
        }

        /**
         * 扣除账户对应的金额
         */
        log.info("tronManageService.handleSharingRatio 开始账户增加对应的金额,增加金额={},账户金额={}", money, amountTrans);
        tAccountService.addAccountMoney(tAccountTrans.getId(), amountTrans, money);
        log.info("tronManageService.handleSharingRatio 增加账户金额成功,增加金额={},账户金额={}", money, amountTrans.add(money));

        /**
         * 异步保存交易记录
         */
        Integer trade_type_commission_sharing = Integer.parseInt(sysDictDataService.getDictValue(TRADE_TYPE, TRADE_TYPE_COMMISSION_SHARING));
        String collection_money = "+".concat(money.toString());
        BigDecimal amount_new_collection = amountTrans.add(money);
        tAccountTradeService.saveAccountTrade(tAccountTrans.getAccountId(), shareUserId, tronTransId, collection_money, trade_type_commission_sharing, amount_new_collection.toString(), 1, null);

    }

    /**
     * 归集TRX
     */
    private void doCollectTronAmountTRX(String fromAddress, String privateKey, String toAddress, String taskId, String collectTime) {
        BigDecimal trxBalanceOf = new BigDecimal(BigDecimal.ZERO.toString());
        try {
            /**
             * 1、获取TRX阈值
             */
            TAntwalletConfig collectTronAmountTrxLimitConfig = tAntwalletConfigService.getById(COLLECT_TRON_AMOUNT_TRX_LIMIT);
            BigDecimal collect_tron_amount_trx_limit = new BigDecimal(collectTronAmountTrxLimitConfig.getPValue());

            /**
             * 2、获取区块链地址上的TRX余额
             */
            trxBalanceOf = tronUtils.getAccountTrxBalance(privateKey, fromAddress);

            /**
             * 3、判断区块链上地址的TRX余额是否达到TRX阈值，没有达到则不归集，达到了则进行归集
             */
            if (trxBalanceOf.compareTo(collect_tron_amount_trx_limit) < 0) {
                log.warn("TronManageService.doCollectTronAmountTrx fromAddress={}没有达到trx阈值,toAddress={},taskId={},collectTime={},collect_tron_amount_limit={},trxBalanceOf={}", fromAddress, toAddress, taskId, collectTime, collect_tron_amount_trx_limit, trxBalanceOf);
                //增加归集记录
                String desc = String.format("{%s}没有达到trx阈值{%s}", fromAddress, collect_tron_amount_trx_limit);
                tTronCollectRecordService.saveTTronCollectRecord(fromAddress, toAddress, TRX, trxBalanceOf.toString(),
                        taskId, collectTime, COLLECT_TRON_STATUS_301, desc, null);

                return;
                //throw new RRException(String.format("{%s}没有达到trx阈值{%s}",fromAddress,collect_tron_amount_trx_limit),ResultCodeEnum.TRUNSFER_TRX_NOT_LIMIT.code);
            }

            /**
             * 4、开始进行TRX归集
             */
            String txid = tronUtils.trunsferTRX(privateKey, fromAddress, toAddress, trxBalanceOf.longValue());

            /**
             * 5、增加归集记录
             */
            tTronCollectRecordService.saveTTronCollectRecord(fromAddress, toAddress, TRX, trxBalanceOf.toString(),
                    taskId, collectTime, COLLECT_TRON_STATUS_100, "trx归集进行中", txid);
            log.info("TronManageService.doCollectTronAmountTrx trx归集进行中,5秒钟可刷新归集是否成功 fromAddress={},toAddress={},taskId={},collectTime={},collect_tron_amount_limit={},trxBalanceOf={},交易ID={}", fromAddress, toAddress, taskId, collectTime, collect_tron_amount_trx_limit, trxBalanceOf, txid);
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rRException = (RRException) e;
                //增加归集记录
                tTronCollectRecordService.saveTTronCollectRecord(fromAddress, toAddress, TRX, trxBalanceOf.toString(),
                        taskId, collectTime, COLLECT_TRON_STATUS_302, rRException.getMsg(), null);
                return;
                //throw rRException;
            } else {
                log.error(String.format("TronManageService.doCollectTronAmountUSDT trx归集失败,fromAddress={%s},toAddress={%s},taskId={%s},collectTime={%s} 具体失败信息:", fromAddress, toAddress, taskId, collectTime), e);
                //增加归集记录
                tTronCollectRecordService.saveTTronCollectRecord(fromAddress, toAddress, TRX, trxBalanceOf.toString(),
                        taskId, collectTime, COLLECT_TRON_STATUS_303, "trx归集失败", null);
                return;
                //throw new RRException(String.format("TronManageService.doCollectTronAmountTrx trx归集失败,fromAddress={%s},toAddress={%s},taskId={%s},collectTime={%s}", fromAddress, toAddress,taskId,collectTime), ResultCodeEnum.SYSTEM_ERROR_500.code);
            }
        }
    }

    /**
     * 归集USDT
     */
    private void doCollectTronAmountUSDT(String fromAddress, String privateKey, String toAddress, String taskId, String collectTime) {
        BigDecimal usdtBalanceOf = new BigDecimal(BigDecimal.ZERO.toString());
        try {
            /**
             * 1、获取USDT阈值
             */
            TAntwalletConfig collectTronAmountUsdtLimitConfig = tAntwalletConfigService.getById(COLLECT_TRON_AMOUNT_USDT_LIMIT);
            BigDecimal collect_tron_amount_usdt_limit = new BigDecimal(collectTronAmountUsdtLimitConfig.getPValue());

            /**
             * 2、获取区块链地址上的USDT余额
             */
            usdtBalanceOf = tronUtils.getAccountUSDTBalance(privateKey, fromAddress);

            /**
             * 3、判断区块链上地址的USDT余额是否达到USDT阈值，没有达到则不归集，达到了则进行归集
             */
            if (usdtBalanceOf.compareTo(collect_tron_amount_usdt_limit) < 0) {
                log.warn("TronManageService.doCollectTronAmountUSDT fromAddress={}没有达到usdt阈值,toAddress={},taskId={},collectTime={},collect_tron_amount_usdt_limit={},usdtBalanceOf={}", fromAddress, toAddress, taskId, collectTime, collect_tron_amount_usdt_limit, usdtBalanceOf);
                //增加归集记录
                String desc = String.format("{%s}没有达到usdt阈值{%s}", fromAddress, collect_tron_amount_usdt_limit);
                tTronCollectRecordService.saveTTronCollectRecord(fromAddress, toAddress, USDT, usdtBalanceOf.toString(),
                        taskId, collectTime, COLLECT_TRON_STATUS_301, desc, null);
                return;
                //throw new RRException(String.format("{%s}没有达到usdt阈值{%s}",fromAddress,collect_tron_amount_usdt_limit),ResultCodeEnum.TRUNSFER_USDT_NOT_LIMIT.code);
            }

            /**
             * 4、开始进行USDT归集
             */
            WithdrawUSDTData withdrawUSDTData = tronUtils.trunsferUSDT(privateKey, fromAddress, toAddress, usdtBalanceOf.toBigInteger());

            /**
             * 5、增加归集记录
             */
            tTronCollectRecordService.saveTTronCollectRecord(fromAddress, toAddress, USDT, usdtBalanceOf.toString(),
                    taskId, collectTime, COLLECT_TRON_STATUS_100, "usdt归集进行中", withdrawUSDTData.getTxid());
            log.info("TronManageService.doCollectTronAmountUSDT usdt归集进行中,5秒钟可刷新归集是否成功 fromAddress={},toAddress={},taskId={},collectTime={},collect_tron_amount_usdt_limit={},usdtBalanceOf={},交易ID={}",
                    fromAddress, toAddress, taskId, collectTime, collect_tron_amount_usdt_limit, usdtBalanceOf, withdrawUSDTData.getTxid());
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rRException = (RRException) e;
                //增加归集记录
                tTronCollectRecordService.saveTTronCollectRecord(fromAddress, toAddress, USDT, usdtBalanceOf.toString(),
                        taskId, collectTime, COLLECT_TRON_STATUS_302, rRException.getMsg(), null);
                return;
                //throw rRException;
            } else {
                log.error(String.format("TronManageService.doCollectTronAmountUSDT usdt归集失败,fromAddress={%s},toAddress={%s},taskId={%s},collectTime={%s} 具体失败信息:", fromAddress, toAddress, taskId, collectTime), e);
                //增加归集记录
                tTronCollectRecordService.saveTTronCollectRecord(fromAddress, toAddress, USDT, usdtBalanceOf.toString(),
                        taskId, collectTime, COLLECT_TRON_STATUS_303, "usdt归集失败", null);
                return;
                //throw new RRException(String.format("TronManageService.doCollectTronAmountUSDT usdt归集失败,fromAddress={%s},toAddress={%s},taskId={%s},collectTime={%s}", fromAddress, toAddress,taskId,collectTime), ResultCodeEnum.SYSTEM_ERROR_500.code);
            }
        }
    }

    @Async
    public void sendTGMessage(String userTGID, String firstName, String userName, String httpName, Map<String, Object> paramMap) {
        String token = createSendMessageToken(userTGID, firstName, userName);
        try {
            httpRequestUtil
                    .doGetRequest(myCommonConfig.getTgHttpUrl().concat("/").concat(httpName), token, paramMap,
                            myCommonConfig.getTgHttpTimeOut());
        } catch (Exception e) {
            log.error(String.format("发送TG消息失败,userTGID=%s,firstName=%s,userName=%s,httpName=%s,paramMap=%s,具体失败信息:"
                    , userTGID, firstName, userName, httpName, JSONUtil.toJsonStr(paramMap)), e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                throw new RRException(rrException.getMsg(), rrException.getCode());
            }
            throw new RuntimeException(e);
        }
    }

    public TgBotUserResult getBotChat(String userTGID, String firstName, String userName, String httpName, Map<String, Object> paramMap,String lang) {
        String token = createSendMessageToken(userTGID, firstName, userName);
        try {
            String body = httpRequestUtil.doGetRequest(myCommonConfig.getTgHttpUrl().concat("/").concat(httpName), token, paramMap,
                    myCommonConfig.getTgHttpTimeOut());
            if (StringUtils.isBlank(body)) {
                log.warn(String.format("获取TG中的用户信息失败,userTGID=%s,firstName=%s,userName=%s,httpName=%s,paramMap=%s"
                        , userTGID, firstName, userName, httpName, JSONUtil.toJsonStr(paramMap)));
                throw new RRException(I18nUtil.getMessage("7001",lang), ResultCodeEnum.TG_HTTP_ERROR.code);
            }
            TgBotUserResult tgBotUserResult = JSONUtil.toBean(body, TgBotUserResult.class);
            return tgBotUserResult;
        } catch (Exception e) {
            log.error(String.format("获取TG中的用户信息失败,userTGID=%s,firstName=%s,userName=%s,httpName=%s,paramMap=%s,具体失败信息:"
                    , userTGID, firstName, userName, httpName, JSONUtil.toJsonStr(paramMap)), e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                throw new RRException(rrException.getMsg(), rrException.getCode());
            }
            throw new RuntimeException(e);
        }
    }

    private String createSendMessageToken(String userTGID, String firstName, String userName) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("ip", NetUtil.getLocalhostStr());
        m.put("userTGID", userTGID);
        m.put("firstName", firstName);
        m.put("userName", userName);
        return hutoolJWTUtil.createTokenHut(m, myCommonConfig.getCommonTokenTimeOut(), myCommonConfig.getCommonTokenSecret());
    }

    private String createWalletQrCode(String base58CheckAddress, String logoPath) throws IOException {
        ByteArrayOutputStream bof = new ByteArrayOutputStream();
        BitMatrix matrix = BitMatrixBuilder.create().setContent(base58CheckAddress).build();
        QRBarCodeUtil.createLogoQrCodeToStream(matrix, bof, Paths.get(logoPath));
        return QRBarCodeUtil.imageToBase64(bof.toByteArray());
    }

    /*private String getRandomBase58CheckAddress(Integer accountType, String money) {
        String base58CheckAddress = "";
        List<TWalletPool> tWalletPoolList = tWalletPoolService.getTWalletPoolList();
        if (tWalletPoolList == null || tWalletPoolList.size() == 0) {
            return base58CheckAddress;
        }
        TWalletPool tWalletPool = tWalletPoolList.get(ThreadLocalRandom.current().nextInt(tWalletPoolList.size()));
        *//**
         * 判断是否有未完成的充值
         *//*
        Integer status_0 = Integer.parseInt(sysDictDataService.getDictValue(CHARGE_STATUS, CHARGE_STATUS_0));
        TCharge tCharge = tChargeService.queryCharge(null, null, base58CheckAddress, accountType, money, status_0);
        String charge_redis = redisOperate.getChargeToRedis(tWalletPool.getBase58CheckAddress(), accountType, money);
        if (StringUtils.isNotBlank(charge_redis) || tCharge != null) {
            base58CheckAddress = getRandomBase58CheckAddress(accountType, money);
        } else {
            base58CheckAddress = tWalletPool.getBase58CheckAddress();
        }

        return base58CheckAddress;
    }*/

    private String getRandomBase58CheckAddress(Integer accountType, String money) {
        String base58CheckAddress = "";
        List<TWalletPool> tWalletPoolList = tWalletPoolService.getTWalletPoolList();
        if (tWalletPoolList == null || tWalletPoolList.size() == 0) {
            return base58CheckAddress;
        }

        int maxAttempts = 20; // 最大尝试次数
        int attempts = 0; // 当前尝试次数

        while (attempts < maxAttempts) {
            attempts++; // 增加尝试次数
            TWalletPool tWalletPool = tWalletPoolList.get(ThreadLocalRandom.current().nextInt(tWalletPoolList.size()));
            Integer status_0 = Integer.parseInt(sysDictDataService.getDictValue(CHARGE_STATUS, CHARGE_STATUS_0));
            TCharge tCharge = tChargeService.queryCharge(null, null, tWalletPool.getBase58CheckAddress(), accountType, money, status_0);
            String charge_redis = redisOperate.getChargeToRedis(tWalletPool.getBase58CheckAddress(), accountType, money);

            if (StringUtils.isNotBlank(charge_redis) || tCharge != null) {
                log.info("有未完成的充值，继续循环--充值地址：{}，charge_redis：{}，tCharge：{}，正在重试", tWalletPool.getBase58CheckAddress(), charge_redis
                        , null != tCharge ? JSONUtil.toJsonStr(tCharge) : null);
                // 如果有未完成的充值，继续循环
                continue;
            } else {
                // 找到有效的钱包地址，退出循环
                base58CheckAddress = tWalletPool.getBase58CheckAddress();
                break;
            }


        }

        // 如果超过最大尝试次数，返回空字符串或其他适当的值
        if(attempts >=maxAttempts){
            log.warn("未能找到有效的 base58CheckAddress ，accountType={}，money={}，在 {} 次 尝试后退出",accountType, money,maxAttempts);
        }

        return base58CheckAddress;
    }


    /**
     * 监控充值活动,并增加到虚拟账户上
     *
     * @param tCharge
     */
    private void addChargeToAccount(TCharge tCharge, TUser chargeTUser) {
        try {
            /**
             * 1、监控TRX或者USDT充值，获取充值金额
             */
            TransferChargeData chargeRecordData = null;
            String chargeType = sysDictDataService.getDictLabel(ACCOUNT_TYPE, tCharge.getChargeType().toString());
            if (chargeType.equals(TRX)) {
                chargeRecordData = getTRXCharge(tCharge);
            }
            if (chargeType.equals(USDT)) {
                chargeRecordData = getUSDTCharge(tCharge);
            }

            /**
             * 2、判断是否有充值
             */
            if (chargeRecordData == null) {
                log.warn("addChargeToAccount orderId={},address={}没有监控到充值", tCharge.getOrderId(),
                        tCharge.getBase58CheckAddress());
                return;
            }

            /**
             * 3、修改充值订单状态为已充值
             */
            Integer status_1 = Integer.parseInt(sysDictDataService.getDictValue(CHARGE_STATUS, CHARGE_STATUS_1));
            tChargeService.modifyChargeStatus(tCharge.getId(), status_1, chargeRecordData.getMoney(),
                    chargeRecordData.getChargeType(), chargeRecordData.getChargeTime(),
                    chargeRecordData.getChargeTxid());

            /**
             * 4、删除充值redis和取消充值次数的redis
             */
            redisOperate.removeChargeToRedis(tCharge.getBase58CheckAddress(), tCharge.getChargeType(), tCharge.getMoney());
            redisOperate.removeCancelChargeToRedis(chargeTUser.getTgId());

            /**
             * 5、往对应的账户上添加充值金额
             */
            saveChargeToAccount(chargeTUser, tCharge.getMoney(), chargeRecordData.getChargeType(), tCharge.getOrderId());
        } catch (Exception e) {
            log.error("addChargeToAccount 监控充值失败,具体失败信息:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 监控TRX充值
     *
     * @param tCharge
     */
    private TransferChargeData getTRXCharge(TCharge tCharge) {
        /**
         * 1、判断是否有TRX充值
         *
         */
        // 转换为北京时间的ZonedDateTime
        long createTime = tCharge.getCreateTime().atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
        long expireTime = tCharge.getExpireTime().atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
        TronTRXResponse<TransferTRXData> transferTRXResponse = tronUtils.getTransferTRXRecordByAddress
                (tCharge.getBase58CheckAddress(), createTime, expireTime);
        if (transferTRXResponse == null) {
            log.warn("getTRXCharge address={}没有TRX充值", tCharge.getBase58CheckAddress());
            return null;
        }
        if (transferTRXResponse.getData() == null || transferTRXResponse.getData().size() == 0) {
            log.warn("getTRXCharge address={} TRX充值没有到账,response={}", tCharge.getBase58CheckAddress(), JSONUtil.toJsonStr(transferTRXResponse));
            return null;
        }

        /**
         * 2、获取充值记录
         */
        TransferTRXData transferTRXData = null;

        List<TransferTRXData> token_transfers = transferTRXResponse.getData();
        for (TransferTRXData trxData : token_transfers) {
            /**
             * 3、判断是否充值成功
             */
            if (!trxData.getContractRet().equals("SUCCESS")) {
                continue;
            }
            /**
             * 4、判断充值金额是否满足
             */
            BigDecimal chargeMoney = new BigDecimal(tCharge.getMoney());
            BigDecimal divisor = new BigDecimal("1000000");
            chargeMoney = chargeMoney.multiply(divisor);
            chargeMoney = chargeMoney.setScale(0, RoundingMode.DOWN);
            BigDecimal amount = new BigDecimal(trxData.getAmount());
            if (!amount.equals(chargeMoney)) {
                continue;
            }
            transferTRXData = trxData;
        }

        /**
         * 5、判断是否有充值记录
         */
        if (transferTRXData == null) {
            log.warn("getTRXCharge charge={}充值失败,没有充值记录,response={}", JSONUtil.toJsonStr(tCharge), JSONUtil.toJsonStr(transferTRXResponse));
            return null;
        }

        /*TransferTRXData transferTRXData = transferTRXResponse.getData().get(0);

         *//**
         * 2、判断TRX充值是否是在充值订单时间内
         *//*
    long trxTimeStamp = transferTRXData.getTimestamp();
    if(trxTimeStamp <= createTime || trxTimeStamp >= expireTime ){
      log.warn("getTRXCharge address={} TRX充值已过期,createTime={},expireTime={},trxTimeStamp={},response={}",
          tCharge.getBase58CheckAddress(),createTime,expireTime,trxTimeStamp, JSONUtil.toJsonStr(transferTRXResponse));
      return null;
    }

    *//**
         * 3、判断是否充值成功
         *//*
    if(!transferTRXData.getContractRet().equals("SUCCESS")){
      log.warn("getTRXCharge address={}充值失败,response={}",tCharge.getBase58CheckAddress(), JSONUtil.toJsonStr(transferTRXResponse));
      return null;
    }

    *//**
         * 4、判断充值金额是否满足
         *//*
    BigDecimal chargeMoney = new BigDecimal(tCharge.getMoney());
    BigDecimal divisor = new BigDecimal("1000000");
    chargeMoney = chargeMoney.multiply(divisor);
    BigDecimal amount = new BigDecimal(transferTRXData.getAmount());
    if(!amount.equals(chargeMoney)){
      log.warn("getTRXCharge charge={}充值失败,充值金额不一致,chargeMoney={},amount={},response={}",JSONUtil.toJsonStr(tCharge),
          chargeMoney,transferTRXData.getAmount(), JSONUtil.toJsonStr(transferTRXData));
      return null;
    }
*/
        /**
         * 5、返回监控充值记录
         */
        Integer accountType = Integer.parseInt(sysDictDataService.getDictValue(ACCOUNT_TYPE, TRX));
        TransferChargeData transferChargeData = new TransferChargeData();
        transferChargeData.setAddress(transferTRXData.getTransferToAddress());
        transferChargeData.setMoney(tCharge.getMoney());
        transferChargeData.setChargeType(accountType);
        transferChargeData.setChargeTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(transferTRXData.getTimestamp()), ZoneId
                .systemDefault()));
        transferChargeData.setChargeTxid(transferTRXData.getTransactionHash());

        return transferChargeData;

    }


    /**
     * 监控USDT充值上
     *
     * @param tCharge
     */
    private TransferChargeData getUSDTCharge(TCharge tCharge) {
        /**
         * 1、判断是否有USDT充值
         */
        // 转换为北京时间的ZonedDateTime
        long createTime = tCharge.getCreateTime().atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
        long expireTime = tCharge.getExpireTime().atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
        TronUSDTResponse<TransferUSDTData> transferUSDTResponse = tronUtils.
                getTransferUSDTRecordByAddress(tCharge.getBase58CheckAddress(), createTime, expireTime);
        if (transferUSDTResponse == null) {
            log.warn("getUSDTCharge charge={}, 没有USDT充值", JSONUtil.toJsonStr(tCharge));
            return null;
        }
        if (transferUSDTResponse.getToken_transfers() == null || transferUSDTResponse.getToken_transfers().size() == 0) {
            log.warn("getUSDTCharge charge={} USDT充值没有到账,response={}", JSONUtil.toJsonStr(tCharge), JSONUtil.toJsonStr(transferUSDTResponse));
            return null;
        }

        /**
         * 2、获取充值记录
         */
        TransferUSDTData transferUSDTData = null;

        List<TransferUSDTData> token_transfers = transferUSDTResponse.getToken_transfers();
        for (TransferUSDTData usdtData : token_transfers) {
            /**
             * 3、判断是否充值成功
             */
            if (!usdtData.getContractRet().equals("SUCCESS")) {
                continue;
            }
            /**
             * 4、判断充值金额是否满足 todo 充值问题
             */
            BigDecimal chargeMoney = new BigDecimal(tCharge.getMoney());
            BigDecimal divisor = new BigDecimal("1000000");
            chargeMoney = chargeMoney.multiply(divisor);
            chargeMoney = chargeMoney.setScale(0, RoundingMode.DOWN);
            BigDecimal amount = new BigDecimal(usdtData.getQuant());
            if (!amount.equals(chargeMoney)) {
                continue;
            }
            transferUSDTData = usdtData;
        }

        /**
         * 5、判断是否有充值记录
         */
        if (transferUSDTData == null) {
            log.warn("getUSDTCharge charge={}充值失败,没有USDT充值记录,response={}", JSONUtil.toJsonStr(tCharge), JSONUtil.toJsonStr(transferUSDTResponse));
            return null;
        }

        /**
         * 2、判断USDT充值是否是在充值订单时间内
         *//*
    long blockTs = transferUSDTData.getBlock_ts();
    if(blockTs <= createTime || blockTs >= expireTime ){
      log.warn("getUSDTCharge charge={},createTime={},expireTime={},trxTimeStamp={},response={} USDT充值已过期",
          JSONUtil.toJsonStr(tCharge),createTime,expireTime,blockTs, JSONUtil.toJsonStr(transferUSDTResponse));
      return null;
    }

    *//**
         * 3、判断是否充值成功
         *//*
    if(!transferUSDTData.getContractRet().equals("SUCCESS")){
      log.warn("getUSDTCharge charge={}充值失败,response={}",JSONUtil.toJsonStr(tCharge), JSONUtil.toJsonStr(transferUSDTResponse));
      return null;
    }

    *//**
         * 4、判断充值金额是否满足
         *//*
    BigDecimal chargeMoney = new BigDecimal(tCharge.getMoney());
    BigDecimal divisor = new BigDecimal("1000000");
    chargeMoney = chargeMoney.multiply(divisor);
    if(!transferUSDTData.getQuant().equals(chargeMoney)){
      log.warn("getUSDTCharge charge={}充值失败,充值金额不一致,chargeMoney={},quant={},response={}",JSONUtil.toJsonStr(tCharge),
          chargeMoney,transferUSDTData.getQuant(), JSONUtil.toJsonStr(transferUSDTResponse));
      return null;
    }
*/
        /**
         * 5、返回监控充值记录
         */
        Integer accountType = Integer.parseInt(sysDictDataService.getDictValue(ACCOUNT_TYPE, USDT));
        TransferChargeData transferChargeData = new TransferChargeData();
        transferChargeData.setAddress(transferUSDTData.getTo_address());
        transferChargeData.setMoney(tCharge.getMoney());
        transferChargeData.setChargeType(accountType);
        transferChargeData.setChargeTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(transferUSDTData.getBlock_ts()), ZoneId
                .systemDefault()));
        transferChargeData.setChargeTxid(transferUSDTData.getTransaction_id());

        return transferChargeData;
    }

    private void saveChargeToAccount(TUser chargeTUser, String money, Integer accountType, String tradeNo) {
        /**
         * 1、通过userTGID获取对应的账户
         */
        TAccount tAccount = tAccountService.getAccountByUserIdAndAccountType(chargeTUser.getTgId(), accountType);
        if (tAccount == null) {
            log.warn("saveChargeToAccount userId={},money={},accountType={},tradeNo={} 没有查到用户的账户信息", chargeTUser.getUserId(), money, accountType, tradeNo);
            throw new RRException("没有查到您的账户信息,请确认自己是否有对应的账户!", ResultCodeEnum.ACCOUNT_IS_NOT_EXIST.code);
        }

        /**
         * 2、获取领取的金额和账户金额
         */
        String accountTypeLable = sysDictDataService.getDictLabel(ACCOUNT_TYPE, String.valueOf(accountType));
        BigDecimal add_money;
        try {
            add_money = new BigDecimal(money);
        /*BigDecimal divisor = new BigDecimal("1000000");
        add_money = bigDecimal_money.divide(divisor);*/
        } catch (Exception e) {
            log.error(String.format("saveChargeToAccount userId=%s,money=%s,accountType=%s,tradeNo=%s 充值金额的格式有问题", chargeTUser.getUserId(), money, accountType, tradeNo), e);
            throw new RRException("充值金额的格式错误", ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(tAccount.getAmount());
        } catch (Exception e) {
            log.error(String.format("saveChargeToAccount userId=%s,money=%s,accountType=%s,tradeNo=%s,amount=%s 账户金额的格式有问题", chargeTUser.getUserId(), money, accountType, tradeNo, tAccount.getAmount()), e);
            throw new RRException("账户金额的格式有问题,应该是小数点后面两位!",
                    ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
        }

        /**
         * 3、领取账户增加对应的金额
         */
        log.info("saveChargeToAccount 开始账户增加对应的金额,增加金额={},账户金额={}", add_money, amount);
        tAccountService.addAccountMoney(tAccount.getId(), amount, add_money);
        log.info("saveChargeToAccount 增加账户金额成功,增加金额={},账户金额={}", add_money, amount.add(add_money));

        /**
         * 4、异步保存交易记录
         */
        Integer tradeType = Integer.parseInt(sysDictDataService.getDictValue(TRADE_TYPE, TRADE_TYPE_CHARGE));
        String trade_money = "+".concat(add_money.toString());
        BigDecimal amount_new = amount.add(add_money);
        tAccountTradeService.saveAccountTradeCharge(tAccount.getAccountId(), chargeTUser.getUserId(), tradeNo, trade_money, tradeType, amount_new.toString(), accountType);

        /**
         * 5、通知TG
         */
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("accountType", accountTypeLable);
        paramMap.put("money", add_money);
        paramMap.put("lang", chargeTUser.getLanguage());
        try {
            sendTGMessage(chargeTUser.getTgId(), chargeTUser.getName(),
                    chargeTUser.getNick(), "sendOTronCharge", paramMap);
        } catch (Exception e) {
            throw new RRException("进行充值,通知TG失败", ResultCodeEnum.TG_HTTP_ERROR.code);
        }

    }
}
