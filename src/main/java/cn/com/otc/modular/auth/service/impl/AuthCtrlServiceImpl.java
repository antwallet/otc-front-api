package cn.com.otc.modular.auth.service.impl;

import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.redis.RedisOperate;
import cn.com.otc.common.utils.*;
import cn.com.otc.modular.auth.service.AuthCtrlService;
import cn.com.otc.modular.dict.service.SysDictDataService;
import cn.com.otc.modular.auth.entity.vo.LoginUserVO;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.*;
import cn.com.otc.modular.sys.service.*;
import cn.hutool.core.net.NetUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:登录相关操作
 * @author: zhangliyan
 * @time: 2024/2/21
 */
@Slf4j
@Service
public class AuthCtrlServiceImpl implements AuthCtrlService {

    @Autowired
    private RedisOperate redisOperate;
    @Autowired
    private TUserService tUserService;

    @Autowired
    private TLoginRecordService tLoginRecordService;

    @Autowired
    private TAccountService tAccountService;

    @Autowired
    private SysDictDataService sysDictDataService;

    @Autowired
    private TInvitedUserService tInvitedUserService;

    @Autowired
    private TAccountTradeService tAccountTradeService;

    @Autowired
    private CheckTokenUtil checkTokenUtil;
    @Autowired
    private MyCommonConfig myCommonConfig;
    @Autowired
    private HttpRequestUtil httpRequestUtil;
    @Autowired
    private PhotoFilePathUtils photoFilePathUtils;
    @Autowired
    private HutoolJWTUtil hutoolJWTUtil;
    @Autowired
    private ImageUtil imageUtil;
    @Autowired
    private AESUtils aesUtils;


    private static final String LOGIN_CHANNEL_TG = "TG";
    private static final String ACCOUNT_TYPE = "account_type";//账户类型
    private static final String TRX = "TRX";
    private static final String USDT = "USDT";

    private static final String TRADE_TYPE = "trade_type";
    private static final String TRADE_TYPE_RECEIVE_REDPACKET = "收红包";
    private static final String DEFAULT_PASSWORD = "123456"; // 默认密码


    // 阿里云 OSS 配置

    private static final String OSS_BUCKET_NAME = "packet-gift";


    /**
     * 根据token获取用户信息
     *
     * @param token
     * @param remortIP
     * @param isPremium
     * @param deviceModel
     * @param registrationDate
     * @return
     */
    @Override
    public UserInfoResult reqUserInfoByToken(String token, Boolean isGetAvatar, String remortIP, Boolean isPremium,
                                             String deviceModel, String registrationDate, String lang) {
        /**
         * 1、获取token中有关用户的信息
         */
        UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
        String jsonStr = JSONUtil.toJsonStr(userInfoResult);
        log.info("AuthCtrlService.reqUserInfoByToken 开始获取用户信息 " +
                "userInfoResult={},remortIP:{},isPremium:{},deviceModel:{},registrationDate:{}",
                jsonStr, remortIP, isPremium, deviceModel, registrationDate);

        /**
         * 2、获取数据库中的用户信息
         */
        TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());

        /**
         * 3、判断用户是否注册，没有注册则进行注册操作
         */
        String userId = "";
        String redpacketCover = "";
        String avatar;
        boolean isSetChargePsw = false;
        if (tUser == null) {
            throw new RRException(I18nUtil.getMessage("4015", lang), ResultCodeEnum.USER_IS_NOT_EXIST.code);
        } else {
            avatar = tUser.getAvatar();
            redpacketCover = tUser.getRedpacketCoverImg();
            if (StringUtils.isNotBlank(tUser.getChargePsw())) {
                isSetChargePsw = true;
            }
            if (tUser.getIsshowPanel().equals((short) 1)) {
                userInfoResult.setIsshowPanel(true);
            } else {
                userInfoResult.setIsshowPanel(false);
            }

            log.info("AuthCtrlService.reqUserInfoByToken 结束获取用户信息 userInfoResult={}", jsonStr);
            //用户不为空就修改一下数据
            //tUserService.updateUser(tUser.getId(), userInfoResult.getFirstName(), userInfoResult.getUserName());
            userInfoResult.setUserId(tUser.getUserId());
        }
        userInfoResult.setRedpacketCoverImg(redpacketCover);
        userInfoResult.setSetChargePsw(isSetChargePsw);
        userInfoResult.setAvatar(avatar);

        log.info("结束获取用户信息 userInfoResult={}", jsonStr);
        return userInfoResult;
    }



    private String fetchAndSetAvatar(UserInfoResult userInfoResult, boolean isGetAvatar) {
        String avatar = "";
        if (isGetAvatar) {
            try {
                String body = sendTGMessage(userInfoResult.getUserTGID(), userInfoResult.getFirstName(),
                        userInfoResult.getUserName(), "getUserProfilePhoto", null);
                Map<String, Object> tgResultMap = JSONUtil.toBean(body, Map.class);
                if (tgResultMap != null && tgResultMap.get("avatar") != null) {
                    String image = tgResultMap.get("avatar").toString();
//                    String s = imageUtil.imageToBase64(myCommonConfig.getTgHttpAntwalletbot().concat(image));
                    byte[] bytes = photoFilePathUtils.downloadAvatar(myCommonConfig.getTgHttpApiFilePath().concat(image));
                    avatar = photoFilePathUtils.uploadToOss(bytes, image);
                }
            } catch (Exception e) {
                log.info("获取头像失败,通知TG失败", e);
//                throw new RRException("获取头像失败,通知TG失败", ResultCodeEnum.TG_HTTP_ERROR.code);
                return "";
            }
        }
        return avatar;
    }



    /**
     * 第一次领取红包时首次绑定到发红包的用户上
     *
     * @param token
     * @return
     */
    /*@Override
    public UserInfoResult invitedUser(String token, String redpacketId, String lang) {
        *//**
         * 1、获取token中有关用户的信息
         *//*
        UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
        try {
            log.info("AuthCtrlService.invitedUser 开始获取用户信息 userInfoResult={},redpacketId={}",
                    JSONUtil.toJsonStr(userInfoResult), redpacketId);

            *//**
             * 2、获取数据库中的用户信息
             *//*
            TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());

            *//**
             * 4、判断用户是否注册，没有注册则进行注册操作
             *//*
            String userId = "";
            String redpacketCover = "";
            String avatar = "";
            boolean isSetChargePsw = false;
            if (tUser == null) {
                throw new RRException(I18nUtil.getMessage("4015", lang), ResultCodeEnum.USER_IS_NOT_EXIST.code);
                *//*log.info("AuthCtrlService.invitedUser 用户开始注册,注册的相关信息:userInfoResult:{}", JSONUtil.toJsonStr(userInfoResult));
                userId = "user_".concat(UidGeneratorUtil.genId());//根据雪花算法获取用户id
                tUserService
                        .registerTUser(userId, userInfoResult.getUserTGID(),
                                userInfoResult.getFirstName(), userInfoResult.getUserName(),
                                null, EncryptUtil.encryptByBCrypt(DEFAULT_PASSWORD), "", false, "", "");
                *//**//**
                 * 5、保存到用户邀请关系表中
                 *//**//*
                bindInvitedUser(userId, redpacketId);

                *//**//**
                 * 6、保存用户账户信息，分别保存TRX和USDT账户
                 *//**//*
                Map<String, Object> accountTypeMap = sysDictDataService.getSysDictByType(ACCOUNT_TYPE);
                for (Map.Entry<String, Object> entry : accountTypeMap.entrySet()) {
                    String accountId = "account_".concat(UidGeneratorUtil.genId());//根据雪花算法获取账户id
                    Integer accountType = Integer.parseInt((String) entry.getValue());
                    tAccountService
                            .saveUserAccount(accountId, accountType, userInfoResult.getUserTGID(), "0.00");
                }
                redpacketCover = myCommonConfig.getCommonRedpacketCoverDefaultPath();
                log.info("AuthCtrlService.invitedUser 用户结束注册,注册的相关信息:userInfoResult:{}", JSONUtil.toJsonStr(userInfoResult));
                *//**//**
                 * 7、保存登录记录
                 *//**//*
                tLoginRecordService.saveLoginRecord(userId, LOGIN_CHANNEL_TG);*//*
            } else {

                redpacketCover = tUser.getRedpacketCoverImg();
                if (StringUtils.isNotBlank(tUser.getChargePsw())) {
                    isSetChargePsw = true;
                }
                *//**
                 * 8、当用户第一次领取红包时保存到用户邀请关系表中
                 *//*
                Integer tradeType = Integer.parseInt(sysDictDataService.getDictValue(TRADE_TYPE, TRADE_TYPE_RECEIVE_REDPACKET));
                TAccountTrade accountTrade = tAccountTradeService.selectAccountTradeByUserId(tUser.getUserId(), tradeType);
                if (accountTrade == null) {
                    bindInvitedUser(tUser.getUserId(), redpacketId);
                }
            }

            userInfoResult.setRedpacketCoverImg(redpacketCover);
            userInfoResult.setSetChargePsw(isSetChargePsw);

            log.info("AuthCtrlService.invitedUser 结束获取用户信息 userInfoResult={}", JSONUtil.toJsonStr(userInfoResult));
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rRException = (RRException) e;
                throw rRException;
            } else {
                throw new RRException(String.format("AuthCtrlService.invitedUser 获取用户信息失败,redpacketId={%s},userInfoResult={%s}", redpacketId, JSONUtil.toJsonStr(userInfoResult)), ResultCodeEnum.SYSTEM_ERROR_500.code, e);
            }
        }
        return userInfoResult;
    }*/

    @Override
    public void resetChargePsw(String userTGId, String chargePassword, String lang) {
        LambdaQueryWrapper<TUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TUser::getTgId, userTGId);
        TUser tUser = tUserService.getOne(lambdaQueryWrapper);
        if (tUser == null) {
            log.warn("AuthCtrlService.resetChargePsw 修改支付密码失败,用户不存在,userTGId={}", userTGId);
            throw new RRException(I18nUtil.getMessage("4002", lang), ResultCodeEnum.USER_IS_NOT_EXIST.code);
        }
        //94E113C6A898CD39  16位密钥
        String decrypt = aesUtils.decrypt(chargePassword, "94E113C6A898CD39");
        if (EncryptUtil.checkPswByBCrypt(decrypt, tUser.getChargePsw())) {
            log.warn("AuthCtrlService.resetChargePsw 修改支付密码失败,密码和老密码一致,userTGId={}", userTGId);
            throw new RRException(I18nUtil.getMessage("6001", lang), ResultCodeEnum.USER_PSW_EXIST.code);
        }
        tUser.setChargePsw(EncryptUtil.encryptByBCrypt(decrypt));
        tUser.setUpdateTime(LocalDateTime.now());
        tUserService.updateById(tUser);
    }

    @Override
    public void registerUser(String userTGId, String logiName, String chargePassword, String lang) {
        LambdaQueryWrapper<TUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TUser::getTgId, userTGId);
        TUser tUser = tUserService.getOne(lambdaQueryWrapper);
        if (tUser == null) {
            log.warn("AuthCtrlService.registerUser 注册账号失败,用户不存在,userTGId={}", userTGId);
            throw new RRException(I18nUtil.getMessage("1012", lang), ResultCodeEnum.USER_IS_NOT_EXIST.code);
        }

        if (StringUtils.isNotBlank(tUser.getLoginName())) {
            log.warn("AuthCtrlService.registerUser 注册账号失败,注册账号已存在,userTGId={},loginName={}", userTGId, tUser.getLoginName());
            throw new RRException(I18nUtil.getMessage("6002", lang), ResultCodeEnum.USER_LOGINNAME_EXIST.code);
        }

        tUser.setLoginName(logiName);
        tUser.setPassword(EncryptUtil.encryptByBCrypt(chargePassword));
        tUser.setUpdateTime(LocalDateTime.now());
        tUserService.updateById(tUser);
    }

    @Override
    public UserInfoResult getUserInfoAvatarToken(String requestToken, Boolean isGetAvatar, String lang) {
        /**
         * 1、获取token中有关用户的信息
         */
        UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(requestToken);
        String jsonStr = JSONUtil.toJsonStr(userInfoResult);
        log.info("AuthCtrlService.getUserInfoAvatarToken 开始获取用户头像 userInfoResult={}", jsonStr);

        /**
         * 2、获取数据库中的用户信息
         */
        TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
        if (tUser == null){
            log.warn("AuthCtrlService.getUserInfoAvatarToken 获取用户信息失败,用户不存在,userTGId={}", userInfoResult.getUserTGID());
            throw new RRException(I18nUtil.getMessage("4002", lang), ResultCodeEnum.USER_IS_NOT_EXIST.code);
        }

        /**
         * 3、判断用户是否注册，没有注册则进行注册操作
         */
        String avatar = "";
        if (StringUtils.isBlank(tUser.getAvatar())) {
            avatar = fetchAndSetAvatar(userInfoResult, isGetAvatar);
            log.info("AuthCtrlService.getUserInfoAvatarToken 头像值,userTGId={},avatar={}", userInfoResult.getUserTGID(), avatar);
            tUserService.updateUserAvatar(tUser.getId(), avatar);
        } else {
            avatar = tUser.getAvatar();
            log.info("AuthCtrlService.getUserInfoAvatarToken 头像值,userTGId={},avatar={}", userInfoResult.getUserTGID(), avatar);
        }

        log.info("AuthCtrlService.getUserInfoAvatarToken 结束获取用户头像 userInfoResult={}", jsonStr);
        userInfoResult.setAvatar(avatar);
        return userInfoResult;
    }
    
    @Override
    public void generateUserInfo(LoginUserVO loginUserVO, String remoteIP, String lang) {
        TUser tUser = tUserService.getTUserByTGId(loginUserVO.getUserTGID());
        log.info("tgId:{},generateUserInfo-用户信息,userScore:{}", loginUserVO.getUserTGID(), JSON.toJSONString(tUser));

        //用户存在,返回
        if (null != tUser) {
            return;
        }
        log.info("tgId:{},generateUserInfo-开始生成用户信息", loginUserVO.getUserTGID());
        String userId = "user_".concat(UidGeneratorUtil.genId());//根据雪花算法获取用户id

        tUserService.registerTUser(userId, loginUserVO.getUserTGID(),
                loginUserVO.getFirstName(), loginUserVO.getUserName(),
                StringUtils.EMPTY, EncryptUtil.encryptByBCrypt(DEFAULT_PASSWORD), remoteIP, loginUserVO.getIsPremium(), loginUserVO.getDeviceModel()
                , loginUserVO.getRegistrationDate(), BigDecimal.ZERO,lang,loginUserVO.getResolution(),loginUserVO.getNetworkStatus(),loginUserVO.getNetworkType());
        /**
         * 5、保存用户账户信息，分别保存TRX和USDT账户
         */
        Map<String, Object> accountTypeMap = sysDictDataService.getSysDictByType(ACCOUNT_TYPE);
        for (Map.Entry<String, Object> entry : accountTypeMap.entrySet()) {
            String accountId = "account_".concat(UidGeneratorUtil.genId());//根据雪花算法获取账户id
            Integer accountType = Integer.parseInt((String) entry.getValue());
            tAccountService.saveUserAccount(accountId, accountType, loginUserVO.getUserTGID(), "0");
        }


        log.info("generateUserInfo-保存登录记录");
    }

    @Override
    public void handleUpdateUserInfo(LoginUserVO loginUserVO, String remortIP, String lang) {
        TUser tUser = tUserService.getTUserByTGId(loginUserVO.getUserTGID());
        if (null == tUser){
            return;
        }
        /**
         * 1、修改用户的信息
         */
        //tUserService.handleUpdateUserInfo(loginUserVO, remortIP, tUser.getId(), lang);
        /**
         * 2、修改用户的评分
         */
        /*ArrayList<String> strings = new ArrayList<>();
        strings.add("resolution");
        strings.add("networkType");
        strings.add("networkStatus");
        List<TUserScore> tUserScore = tUserScoreService.getTUserScore(loginUserVO.getUserTGID(), strings);
        if (CollectionUtils.isEmpty(tUserScore) || tUserScore.size()<3){
            BigDecimal userScore = getUserScore(loginUserVO);
            TUser tempUser = new TUser();
            tempUser.setId(tUser.getId());
            tempUser.setUserScore(userScore);
            tempUser.setUpdateTime(LocalDateTime.now());
            tUserService.updateUserScore(tempUser);
        }*/

        /**
         * 3、修改最后一次登录的记录
         */
        tLoginRecordService.updateLoginRecord(loginUserVO,remortIP,lang,tUser);
    }

    private String sendTGMessage(String userTGID, String firstName, String userName, String httpName, Map<String, Object> paramMap) {
        String token = createSendMessageToken(userTGID, firstName, userName);
        try {
            return httpRequestUtil
                    .doGetRequest(myCommonConfig.getTgHttpUrl().concat("/").concat(httpName), token, paramMap,
                            myCommonConfig.getTgHttpTimeOut());
        } catch (Exception e) {
            log.error(String.format("发送TG消息失败,userTGID=%s,firstName=%s,userName=%s,httpName=%s,paramMap={},具体失败信息:"
                    , userTGID, firstName, userName, httpName, JSONUtil.toJsonStr(paramMap)), e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                throw new RRException(rrException.getMsg(), rrException.getCode());
            }
            throw new RuntimeException(e);
        }
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
