package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.CheckTokenUtil;
import cn.com.otc.common.utils.HttpStatus;
import cn.com.otc.common.utils.I18nUtil;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.bean.pojo.TUserBuyPremium;
import cn.com.otc.modular.sys.service.TUserBuyPremiumService;
import cn.com.otc.modular.sys.service.TUserService;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/4
 */
@Slf4j
@RestController
@RequestMapping("/api/front/premium")
public class TUserBuyPremiumController {

    @Autowired
    private TUserService tUserService;
    @Autowired
    private TUserBuyPremiumService tUserBuyPremiumService;
    @Autowired
    private CheckTokenUtil checkTokenUtil;

    /**
     * 根据token获取用户购买会员信息
     *
     * @return
     */
    @RequestMapping("/list")
    public R list(HttpServletRequest httpRequest) {
        String lang = httpRequest.getHeader("lang");
        if (StringUtils.isBlank(lang)) {
            lang = "en-US";
        }
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
                log.info("TUserBuyPremiumController.list 用户信息不存在,userInfoResult={}", JSONUtil.toJsonStr(userInfoResult));
                return R.error(ResultCodeEnum.USER_IS_NOT_EXIST.code, I18nUtil.getMessage("4002", lang));
            }

            List<TUserBuyPremium> list = tUserBuyPremiumService.list(tUser);
            return R.ok().put("list",list);
        } catch (Exception e) {
            log.error("根据token获取用户购买会员信息,具体失败信息:", e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, I18nUtil.getMessage("605",lang));
        }
    }
    /**
     * 根据token获取用户信息
     *
     * @return
     */
    @RequestMapping("/getChat/{userName}")
    public ResponseEntity<?> getChat(HttpServletRequest httpRequest, @PathVariable("userName") String userName) {
        String lang = httpRequest.getHeader("lang");
        if (StringUtils.isBlank(lang)) {
            lang = "en-US";
        }
        try {

            /**
             * 1、根据token获取用户信息
             */
            return tUserBuyPremiumService.getChat(userName, lang);
        } catch (Exception e) {
            log.error("根据token获取用户信息,具体失败信息:", e);
            return ResponseEntity.failure(HttpStatus.SC_INTERNAL_SERVER_ERROR, I18nUtil.getMessage("604",lang));
        }
    }

    /**
     * 获取会员类型的价格
     *
     * @return
     */
    @RequestMapping("/handlePremiumType")
    public R handlePremiumType(HttpServletRequest httpRequest, @RequestParam(defaultValue = StringUtils.EMPTY)String exchangeRateType) {
        String lang = httpRequest.getHeader("lang");
        if (StringUtils.isBlank(lang)) {
            lang = "en-US";
        }
        try {
            return tUserBuyPremiumService.handlePremiumType(lang, httpRequest, exchangeRateType);
        } catch (Exception e) {
            log.error("根据token获取用户信息,具体失败信息:", e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, I18nUtil.getMessage("604", lang));
        }
    }


}
