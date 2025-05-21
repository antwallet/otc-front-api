package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.redis.NoRepeatSubmit;
import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.*;
import cn.com.otc.modular.auth.service.AuthCtrlService;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.bean.vo.RegisterUserVO;
import cn.com.otc.modular.sys.service.TUserService;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/2/26
 */
@Slf4j
@RestController
@RequestMapping("/api/front/user")
public class UserCtrlController {

    @Autowired
    private AuthCtrlService authCtrlService;
    @Autowired
    private TUserService tUserService;
    @Autowired
    private CheckTokenUtil checkTokenUtil;
    @Autowired
    private CheckChargePswUtil checkChargePswUtil;
    @Resource
    private SharedCache userMapLocalCache;


    @NoRepeatSubmit()
    @RequestMapping("/resetChargePsw")
    public R resetChargePsw(HttpServletRequest httpRequest, @RequestBody String chargePassword) {
        try {
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isBlank(lang)) {
                lang = "en-US";
            }
            if (StringUtils.isBlank(chargePassword)) {
                log.warn("resetChargePsw:修改支付密码失败,非法参数");
                return R.error(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1000", lang));
            }
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            //解密
            authCtrlService.resetChargePsw(userInfoResult.getUserTGID(), chargePassword, lang);
            return R.ok();
        } catch (Exception e) {
            log.error("修改支付密码失败,具体失败信息:", e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            return R.error();
        }
    }

    /**
     * 验证支付密码
     *
     * @param httpRequest
     * @param chargePassword
     * @return
     */
    @NoRepeatSubmit()
    @RequestMapping("/checkChargePsw")
    public R checkChargePsw(HttpServletRequest httpRequest, @RequestBody String chargePassword) {
        try {
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isBlank(lang)) {
                lang = "en-US";
            }
            if (StringUtils.isBlank(chargePassword)) {
                log.warn("修改支付密码失败,非法参数");
                return R.error(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1000", lang));
            }
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
            if (tUser == null) {
                log.warn("用户不存在,userInfoResult={}", JSONUtil.toJsonStr(userInfoResult));
                return R.error(ResultCodeEnum.USER_IS_NOT_EXIST.code, I18nUtil.getMessage("4002", lang));
            }
            if (!checkChargePswUtil.verifyChargePsw(chargePassword, tUser.getChargePsw())) {
                log.warn("UserCtrlController.checkChargePsw 验证支付密码失败,支付密码错误,userInfoResult={}", JSONUtil.toJsonStr(userInfoResult));
                return R.error(ResultCodeEnum.USER_PSW_ERROR.code, I18nUtil.getMessage("6005", lang));
            }
            return R.ok();
        } catch (Exception e) {
            log.error("验证支付密码失败,具体失败信息:", e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            return R.error();
        }
    }

    @NoRepeatSubmit()
    @RequestMapping("/registerUser")
    public R registerUser(HttpServletRequest httpRequest, @RequestBody RegisterUserVO registerUserVO) {
        try {
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isBlank(lang)) {
                lang = "en-US";
            }
            if (StringUtils.isBlank(registerUserVO.getLoginName()) || StringUtils.isBlank(registerUserVO.getPassword())) {
                log.warn("注册账号失败,非法参数");
                return R.error(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1000", lang));
            }
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            authCtrlService.registerUser(userInfoResult.getUserTGID(), registerUserVO.getLoginName(), registerUserVO.getPassword(), lang);
            return R.ok();
        } catch (Exception e) {
            log.error("注册账号失败,具体失败信息:", e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            return R.error();
        }
    }


    @NoRepeatSubmit()
    @RequestMapping("/updateRedPacketCover")
    public R updateRedPacketCover(HttpServletRequest httpRequest, @RequestBody String redpacketImg) {
        try {
            if (StringUtils.isBlank(redpacketImg)) {
                log.warn("修改红包封面失败,非法参数");
                return R.error(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1000", null));
            }
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);

            TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
            if (tUser == null) {
                log.warn("修改红包封面失败,用户={}不存在", userInfoResult.getUserTGID());
                return R.error(ResultCodeEnum.USER_IS_NOT_EXIST.code, "用户不存在");
            }
            tUserService.updateRedPacketCover(tUser.getId(), redpacketImg);
            return R.ok();
        } catch (Exception e) {
            log.error("修改红包封面失败,具体失败信息:", e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            return R.error();
        }
    }

    @NoRepeatSubmit()
    @RequestMapping("/updateShowPanel")
    public R updateShowPanel(HttpServletRequest httpRequest, @RequestBody Short isShowPanel) {
        try {
            if (isShowPanel == null) {
                log.warn("更新是否展示弹窗,非法参数");
                return R.error(ResultCodeEnum.ILLEGAL_PARAMETER.code, I18nUtil.getMessage("1000", null));
            }
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);

            TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
            if (tUser == null) {
                log.warn("更新是否展示弹窗失败,用户={}不存在", userInfoResult.getUserTGID());
                return R.error(ResultCodeEnum.USER_IS_NOT_EXIST.code, "用户不存在");
            }
            tUserService.updateUserIsShowPanel(tUser.getId(), isShowPanel);
            return R.ok();
        } catch (Exception e) {
            log.error("更新是否展示弹窗失败,具体失败信息:", e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            return R.error();
        }
    }


    /*
     * 重置密码
     * */
    @NoRepeatSubmit()
    @RequestMapping("/resetPassword")
    public R resetPassword(HttpServletRequest httpRequest) {
        try {
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isBlank(lang)) {
                lang = "en-US";
            }

            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            tUserService.resetPassword(userInfoResult, lang);
            return R.ok();
        } catch (Exception e) {
            log.error("修改支付密码失败,具体失败信息:", e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            return R.error();
        }
    }


    /*
     * 修改用户语言
     * */
    @NoRepeatSubmit()
    @RequestMapping("/handleEditUserLanguage")
    public ResponseEntity<?> handleEditUserLanguage(HttpServletRequest httpRequest) {
        String language = httpRequest.getHeader("lang");
        if (StringUtils.isBlank(language)) {
            language = "en-US";
        }
        String token = checkTokenUtil.getRequestToken(httpRequest);
        UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
        try {
            //从缓存中取
            TUser user = userMapLocalCache.get(userInfoResult.getUserTGID());
            if (null == user) {
                user = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
                if (null == user) {
                    log.warn("UserCtrlController.handleEditUserLanguage 用户不存在:{}", JSONUtil.toJsonStr(userInfoResult));
                    //throw new RRException(I18nUtil.getMessage("4015", language), ResultCodeEnum.USER_IS_NOT_EXIST.code);
                    return ResponseEntity.failure(ResultCodeEnum.USER_IS_NOT_EXIST.code, I18nUtil.getMessage("4015", language));
                }
                userMapLocalCache.put(userInfoResult.getUserTGID(), user);
            }
            //当用户主动选择语言的时候，就直接更新用户语言 并且当前用户选择的语言和数据库中的语言不一致的时候，就直接更新用户语言
            if (user.getLanguage() == null || !user.getLanguage().equals(language)) {
                tUserService.updateUserLanguage(user.getId(), language);
            }
            return ResponseEntity.success("update success!");
        } catch (Exception e) {
            log.error("修改用户语言失败,tdID:{}, 具体失败信息:", userInfoResult.getUserTGID(), e);
            return ResponseEntity.failure(ResultCodeEnum.FAIL.code, ResultCodeEnum.FAIL.msg);
        }
    }

}
