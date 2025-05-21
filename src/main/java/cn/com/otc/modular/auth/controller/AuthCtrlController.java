package cn.com.otc.modular.auth.controller;

import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.redis.NoRepeatSubmit;
import cn.com.otc.common.utils.*;
import cn.com.otc.modular.auth.service.AuthCtrlService;
import cn.com.otc.modular.auth.entity.vo.LoginUserVO;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.service.TLoginRecordService;
import cn.com.otc.modular.sys.service.TUserService;
import cn.hutool.core.net.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/2/26
 */
@Slf4j
@RestController
@RequestMapping("/api/front/auth")
public class AuthCtrlController {
    @Resource
    private RedissonClient redissonClient;
    @Autowired
    private TUserService tUserService;
    @Autowired
    private AuthCtrlService authCtrlService;
    @Autowired
    private TLoginRecordService tLoginRecordService;
    @Autowired
    private CheckTokenUtil checkTokenUtil;
    @Autowired
    private HutoolJWTUtil hutoolJWTUtil;
    @Autowired
    private MyCommonConfig myCommonConfig;
    @Autowired
    private TelegramWebAppDataValidator telegramWebAppDataValidator;

    private static final String USERNAME = "未设置";

    /**
     * 根据token获取用户信息
     *
     * @return
     */
    //@AccessLimit(maxCount = 20)
    @NoRepeatSubmit()
    @RequestMapping("/getUserInfo")
    public R reqUserInfoByToken(HttpServletRequest httpRequest, Boolean isGetAvatar,Boolean isPremium,String deviceModel,String registrationDate) {
        try {
            String lang = httpRequest.getHeader("lang");
            if (com.alibaba.excel.util.StringUtils.isEmpty(lang)) {
                lang = "en-US";
            }
            //获取用户的语言
            String remortIP = getRemortIP(httpRequest);
            log.info("getUserInfo 用户的IP地址 remoteAddr:" + remortIP);
            UserInfoResult userInfoResult = authCtrlService.reqUserInfoByToken(checkTokenUtil.getRequestToken(httpRequest),
                    isGetAvatar,remortIP,isPremium,deviceModel,registrationDate, lang);
            return R.ok().put("userInfo", userInfoResult);
        } catch (Exception e) {
            log.error("AuthCtrlController.getUserInfo 根据token获取用户信息失败,具体失败信息:", e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "获取用户信息失败,请联系管理员!");
        }
    }

    /**
     * 根据token获取用户信息
     *
     * @return
     */
    //@AccessLimit(maxCount = 20)
    /*@NoRepeatSubmit()
    @RequestMapping("/invitedUser/{redpacketId}")
    public R invitedUser(HttpServletRequest httpRequest, @PathVariable("redpacketId") String redpacketId) {
        try {
            String lang = httpRequest.getHeader("lang");
            if (com.alibaba.excel.util.StringUtils.isEmpty(lang)) {
                lang = "en-US";
            }
            UserInfoResult userInfoResult = authCtrlService.invitedUser(checkTokenUtil.getRequestToken(httpRequest), redpacketId, lang);
            return R.ok().put("userInfo", userInfoResult);
        } catch (Exception e) {
            log.error("AuthCtrlController.invitedUser 根据token获取用户信息失败,具体失败信息:", e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "获取用户信息失败,请联系管理员!");
        }
    }*/

    /**
     * 登录
     *
     * @param loginUserVO
     * @return
     */
    @NoRepeatSubmit()
    @RequestMapping("/doLogin")
    public R doLogin(@RequestBody LoginUserVO loginUserVO, HttpServletRequest httpRequest) {
        String lang = httpRequest.getHeader("lang");
        if (StringUtils.isBlank(lang)) {
            lang = "en-US";
        }
        String key = "doLogin_lock_" + loginUserVO.getUserTGID();
        RLock lock = redissonClient.getLock(key);
        try {
            TUser tUser = tUserService.getTUser(loginUserVO.getUserTGID());
            if (null != tUser && tUser.getIslock() == 1) {
                log.info("用户被封禁,userTGID:{}", loginUserVO.getUserTGID());
                return R.error(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("4015", lang));
            }
            //获取用户的ip
            String remortIP = getRemortIP(httpRequest);
            log.info("用户的IP地址 remoteAddr:" + remortIP);
            loginUserVO.setUserName(StringUtils.isBlank(loginUserVO.getUserName()) ? USERNAME : loginUserVO.getUserName());
            if (StringUtils.isBlank(loginUserVO.getUserTGID()) || StringUtils.isBlank(loginUserVO.getFirstName()) ||
                    StringUtils.isBlank(loginUserVO.getUserName()) || StringUtils.isBlank(loginUserVO.getData())) {
                log.warn("登录失败,非法参数");
                return R.error(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("4015", lang));
            }

            if (!lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                throw new RRException("系统繁忙，请稍后重试");
            }
            log.info("登录请求参数:{}", loginUserVO);
            /*boolean isValid = telegramWebAppDataValidator.validateMiniAppData(loginUserVO.getData(), loginUserVO.getUserTGID());
            log.info("校验是否成功:{}", isValid);
            if (isValid) {*/
                String token = createToken(loginUserVO.getUserTGID(), loginUserVO.getFirstName(), loginUserVO.getUserName());
                //修改用户数据
                String finalLang = lang;
                authCtrlService.generateUserInfo(loginUserVO, getRemortIP(httpRequest),lang);
                CompletableFuture.runAsync(() -> {authCtrlService.handleUpdateUserInfo(loginUserVO, remortIP, finalLang);});
                return R.ok().put("token", token);
            /*}
            log.info("登录请求参数:userId:{},fistName:{},userName:{},data:{}", loginUserVO.getUserTGID(), loginUserVO.getFirstName(), loginUserVO.getUserName(), loginUserVO.getData());
            return R.error(ResultCodeEnum.ILLEGAL_ACCESS_MINI_PROGRAMS_ERROR.code, I18nUtil.getMessage("601", lang));*/
        } catch (Exception e) {
            log.error("AuthCtrlController.doLogin 登录失败,具体失败信息:", e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, I18nUtil.getMessage("607", lang));
        }finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public String getRemortIP(HttpServletRequest request) {
        if (request.getHeader("x-forwarded-for") == null) {
            return request.getRemoteAddr();
        }
        return request.getHeader("x-forwarded-for");
    }


    /**
     * 根据token获取用户的头像
     *
     * @return
     */
    //@AccessLimit(maxCount = 20)
    @NoRepeatSubmit()
    @RequestMapping("/getUserInfoAvatar")
    public R getUserInfoAvatarToken(HttpServletRequest httpRequest, @RequestBody Boolean isGetAvatar) {
        try {
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isBlank(lang)) {
                lang = "en-US";
            }
            UserInfoResult userInfoResult = authCtrlService.getUserInfoAvatarToken(checkTokenUtil.getRequestToken(httpRequest), isGetAvatar, lang);
            return R.ok().put("userInfo", userInfoResult);
        } catch (Exception e) {
            log.error("AuthCtrlController.getUserInfoAvatar 根据token获取用户信息失败,具体失败信息:", e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "获取用户信息失败,请联系管理员!");
        }
    }

    private String createToken(String userTGID, String firstName, String userName) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("ip", NetUtil.getLocalhostStr());
        m.put("userTGID", userTGID);
        m.put("firstName", firstName);
        m.put("userName", userName);
        return hutoolJWTUtil.createTokenHut(m, myCommonConfig.getCommonTokenTimeOut(), myCommonConfig.getCommonTokenSecret());
    }
}
