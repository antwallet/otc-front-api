package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.utils.*;
import cn.com.otc.modular.dict.service.SysDictDataService;
import cn.com.otc.modular.auth.entity.vo.LoginUserVO;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TPasswordResetLog;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.dao.TUserDao;
import cn.com.otc.modular.sys.service.TLoginRecordService;
import cn.com.otc.modular.sys.service.TPasswordResetLogService;
import cn.com.otc.modular.sys.service.TUserService;
import cn.hutool.core.net.NetUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
@Service
@Slf4j
public class TUserServiceImpl extends ServiceImpl<TUserDao, TUser> implements TUserService {
    @Resource
    private RedissonClient redissonClient;
    @Autowired
    private SysDictDataService sysDictDataService;
    @Autowired
    private MyCommonConfig myCommonConfig;
    @Autowired
    private TLoginRecordService tLoginRecordService;
    @Autowired
    private TPasswordResetLogService tPasswordResetLogService;

    @Autowired
    private HutoolJWTUtil hutoolJWTUtil;
    @Autowired
    private HttpRequestUtil httpRequestUtil;
    @Resource
    private SharedCache userMapLocalCache;

    private static final Random random = new Random();
    private static final String LOWER_CASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";


    private static final String LOGIN_CHANNEL_TG = "TG";
    private static final String SYS_NORMAL_DISABLE_TYPE = "sys_normal_disable";
    private static final String SYS_NORMAL_DISABLE_0 = "正常";
    private static final String SYS_NORMAL_DISABLE_1 = "停用";

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<TUser> queryWrapper = new QueryWrapper<>();
        IPage<TUser> page = this.page(
        new Query<TUser>().getPage(params),
        queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public TUser getTUser(String TGId){
        LambdaQueryWrapper<TUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TUser::getTgId,TGId);
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    public TUser getTUserByTGId(String TGId){
      LambdaQueryWrapper<TUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
      lambdaQueryWrapper.eq(TUser::getTgId,TGId);
      lambdaQueryWrapper.eq(TUser::getIslock,0);
      return this.getOne(lambdaQueryWrapper);
    }

  @Override
  public void registerTUser(String userId, String tgId, String name, String nick, String avatar, String pwd,
                            String remortIP, Boolean isPremium, String deviceModel, String registrationDate,
                            BigDecimal userScore, String lang, String resolution, String networkStatus, String networkType) {
    TUser tUser = new TUser();
    tUser.setUserId(userId);
    tUser.setTgId(tgId);
    tUser.setName(name);
    tUser.setNick(nick);
    tUser.setLoginChannel(LOGIN_CHANNEL_TG);
    tUser.setLoginTime(LocalDateTime.now());
    tUser.setCreateTime(LocalDateTime.now());
    tUser.setRedpacketCoverImg(myCommonConfig.getCommonRedpacketCoverDefaultPath());
    tUser.setAvatar(avatar);
    tUser.setPassword(pwd);
    if (isPremium==null){
        isPremium = false;
    }
    tUser.setIsPremium(isPremium);
    tUser.setIpAddress(StringUtils.isNotBlank(remortIP) ? remortIP : null);
    tUser.setRegistrationDate(StringUtils.isNotBlank(registrationDate) ? registrationDate : null);
    tUser.setDeviceModel(StringUtils.isNotBlank(deviceModel) ? deviceModel : null);
    tUser.setUserScore(userScore);
    tUser.setLanguage(lang);
    tUser.setResolution(resolution);
    tUser.setNetworkStatus(networkStatus);
    tUser.setNetworkType(networkType);
    this.save(tUser);
  }

  @Override
  public void updateRedPacketCover(Long userId, String redpacketImg) {
    TUser tUser = new TUser();
    tUser.setId(userId);
    tUser.setRedpacketCoverImg(redpacketImg);
    tUser.setUpdateTime(LocalDateTime.now());
    this.updateById(tUser);
  }

  @Override
  public void updateUserAvatar(Long userId, String avatar) {
    TUser tUser = new TUser();
    tUser.setId(userId);
    tUser.setAvatar(avatar);
    tUser.setUpdateTime(LocalDateTime.now());
    this.updateById(tUser);
  }

  @Override
  public void updateUserIsShowPanel(Long userId, Short isShowPanel) {
    TUser tUser = new TUser();
    tUser.setId(userId);
    tUser.setIsshowPanel(isShowPanel);
    tUser.setUpdateTime(LocalDateTime.now());
    this.updateById(tUser);
  }

    @Override
    public void resetPassword(UserInfoResult userInfoResult, String lang) {
        //当用户点击按钮的时候，把用户的密码初始化，使用机器人给他发送消息，告知他后，重置密码
        String key = "resetPassword_lock_" + userInfoResult.getUserTGID();
        RLock lock = redissonClient.getLock(key);
        String pwd;
        TUser user;
        try {
            // 获取锁并指定超时时间
            if (!lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                throw new RRException("系统繁忙，请稍后重试");
            }
            pwd = generateRandomPassword(6);
            user = getTUserByTGId(userInfoResult.getUserTGID());
            if (null == user) {
                log.warn("RedPacketManageService.resetPassword 重置密码失败，用户不存在，请联系管理员,userInfoResult={}", JSONUtil.toJsonStr(userInfoResult));
                throw new RRException(I18nUtil.getMessage("1016", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
            }
            Integer i = this.baseMapper.updateUserPwdByTgId(userInfoResult.getUserTGID(), EncryptUtil.encryptByBCrypt(pwd));
            if (i < 0) { //修改失败 ，让他联系管理员
                log.warn("RedPacketManageService.resetPassword 重置密码失败,用户={},具体用户信息={}", JSONUtil.toJsonStr(userInfoResult), JSONUtil.toJsonStr(userInfoResult));
                throw new RRException(I18nUtil.getMessage("607", lang), ResultCodeEnum.REDPACKET_MONEY_LIMIT_ERROR.code);
            }
            CompletableFuture.runAsync(()->{
                sendPasswordResetMessage(userInfoResult,user.getLanguage(),pwd);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 确保释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

  @Override
  public void updateUserLanguage(Long id, String lang) {

  }

  //生成随机密码规则是 a-z 0-9
    public static String generateRandomPassword(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive.");
        }

        if (length < 2) {
            throw new IllegalArgumentException("Length must be at least 2.");
        }

        StringBuilder sb = new StringBuilder(length);

        // 生成第一个字符（大写字母）
        int firstCharIndex = random.nextInt(LOWER_CASE_LETTERS.length());
        sb.append(LOWER_CASE_LETTERS.charAt(firstCharIndex));

        int digit = random.nextInt(NUMBERS.length());
        char repeatedDigit = NUMBERS.charAt(digit);
        for (int i = 1; i < length; i++) {
            sb.append(repeatedDigit);
        }
        return sb.toString();
    }


    private void sendPasswordResetMessage(UserInfoResult userInfoResult, String lang, String pwd) {

        String token = createSendMessageToken(userInfoResult.getUserTGID(), userInfoResult.getFirstName(), userInfoResult.getUserName());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pwd", pwd);
        paramMap.put("lang", lang);
        String body = httpRequestUtil.doGetRequest(myCommonConfig.getTgHttpUrl().concat("/sendPasswordResetMessage"), token, paramMap, myCommonConfig.getTgHttpTimeOut());
        Map<String, Object> tgResultMap = JSONUtil.toBean(body, Map.class);
        if (!tgResultMap.get("code").equals(200)) {
            //记录是否需要重新发送密码重置消息
            TPasswordResetLog tPasswordResetLog = tPasswordResetLogService.checkPasswordResetLog(userInfoResult, lang);
            if (tPasswordResetLog==null){
                tPasswordResetLogService.registerTPasswordResetLog(userInfoResult.getUserTGID(),AESUtils.encrypt(pwd));
            }

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



  @Override
  public TUser getTUserByUserId(String userId) {
      Integer isLock = Integer.parseInt(sysDictDataService.getDictValue(SYS_NORMAL_DISABLE_TYPE,SYS_NORMAL_DISABLE_0));
      LambdaQueryWrapper<TUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
      lambdaQueryWrapper.eq(TUser::getUserId,userId);
      lambdaQueryWrapper.eq(TUser::getIslock,isLock);
      return this.getOne(lambdaQueryWrapper);
  }


}
