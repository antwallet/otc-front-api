package cn.com.otc.modular.tron.controller;

import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.redis.NoRepeatSubmit;
import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.*;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.service.TUserService;
import cn.com.otc.modular.tron.dto.bean.vo.ChargeVO;
import cn.com.otc.modular.tron.dto.bean.vo.TronWithdrawMoneyVO;
import cn.com.otc.modular.tron.dto.bean.vo.UserBuyPremiumVO;
import cn.com.otc.modular.tron.dto.vo.result.TronTransResult;
import cn.com.otc.modular.tron.service.TronManageService;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/18
 */
@Slf4j
@RestController
@RequestMapping("/api/front/order")
public class TronCtrlController {
    @Autowired
    private TronManageService tonManageService;
    @Autowired
    private AESUtils aesUtils;
    @Autowired
    private TUserService tUserService;
    @Autowired
    private CheckTokenUtil checkTokenUtil;
    @Autowired
    private CheckChargePswUtil checkChargePswUtil;
    @Autowired
    private MyCommonConfig myCommonConfig;


    private static final String REDPACKET_TITLE = "恭喜发财，大吉大利";
    private static final String REDPACKET_TITLE_EN = "Wishing you great fortune and prosperity!"; // 英文

    /**
     * 获取充值的二维码
     *
     * @return
     */
    @NoRepeatSubmit()
    @RequestMapping("/getChargeQrCode")
    public ResponseEntity<?> getChargeQrCode(HttpServletRequest httpRequest) {
        try {
            //获取用户的语言
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isEmpty(lang)) {
                lang = "en-US";
            }
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            return tonManageService.getWalletQrCode(userInfoResult.getUserTGID(), lang);
            //return R.ok().put("chargeinfo", result);
        } catch (Exception e) {
            log.error("获取充值的二维码失败,具体失败信息:", e);
            return ResponseEntity.failure(HttpStatus.SC_INTERNAL_SERVER_ERROR, "获取充值的二维码失败,请联系管理员!");
        }
    }

    /**
     * 生成充值钱包地址二维码
     *
     * @return
     */
    //@AccessLimit(maxCount = 20)
    @NoRepeatSubmit()
    @RequestMapping("/createChargeQrCode")
    public ResponseEntity<?> createChargeQrCode(HttpServletRequest httpRequest, @RequestBody ChargeVO chargeVO) {
        try {
            //获取用户的语言
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isEmpty(lang)) {
                lang = "en-US";
            }
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            return tonManageService.createWalletQrCode(userInfoResult.getUserTGID(), chargeVO.getAccountType(), chargeVO.getMoney(), lang);

        } catch (Exception e) {
            log.error(String.format("生成充值钱包地址二维码失败,chargeVO=%s,具体失败信息:", JSONUtil.toJsonStr(chargeVO)), e);
            return ResponseEntity.failure(HttpStatus.SC_INTERNAL_SERVER_ERROR, "生成充值钱包地址二维码失败,请联系管理员!");
        }
    }

    /**
     * 取消充值
     *
     * @return
     */
    //@AccessLimit(maxCount = 20)
    @NoRepeatSubmit()
    @RequestMapping("/cancelTronCharge/{orderId}")
    public R cancelTronCharge(HttpServletRequest httpRequest, @PathVariable("orderId") String orderId) {
        try {
            //获取用户的语言
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isEmpty(lang)) {
                lang = "en-US";
            }
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            tonManageService.cancelTronCharge(userInfoResult.getUserTGID(), orderId,lang);
            return R.ok();
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rRException = (RRException) e;
                return R.error(rRException.getCode(), rRException.getMsg());
            } else {
                log.error(String.format("取消充值失败,orderId=%s,具体失败信息:", orderId), e);
                return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "取消充值失败,请联系管理员!");
            }
        }
    }


    /**
     * 创建收款
     *
     * @param httpRequest
     * @param transAccountVO
     * @return
     */
    /*@NoRepeatSubmit()
    @RequestMapping("/createOTronTrans")*/
   /* public R createOTronTrans(HttpServletRequest httpRequest, @RequestBody TransAccountVO transAccountVO) {
        //获取用户的语言
        String lang = httpRequest.getHeader("lang");
        if (StringUtils.isEmpty(lang)) {
            lang = "en-US";
        }
        try {
            String token = checkTokenUtil.getRequestToken(httpRequest);

            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);


            //如果留言为空，则默认24小时
            if (StringUtils.isBlank(transAccountVO.getComment())) {
                transAccountVO.setComment(lang.equals("en-US") ? REDPACKET_TITLE_EN : REDPACKET_TITLE);
            }
            //如果收款过期时间为空，则默认24小时
            if (transAccountVO.getPaymentTimeout()==null) {
                transAccountVO.setPaymentTimeout(24);
            }
            //如果订阅时间为空，则默认24小时
            if (transAccountVO.getSubscriptionTimeout()==null) {
                transAccountVO.setSubscriptionTimeout(24);
            }

            return R.ok().put("link",tonManageService.createOTronTrans(transAccountVO, userInfoResult,lang));
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            log.error(String.format("创建收款失败,transAccountVO=%s,具体失败信息:", JSONUtil.toJsonStr(transAccountVO)), e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, I18nUtil.getMessage("607",lang));
        }
    }*/

   /* @NoRepeatSubmit()
    @RequestMapping("/handleTronTransAccount/{tronTransId}")*/
    /*public R handleTronTransAccount(HttpServletRequest httpRequest, @PathVariable("tronTransId") String tronTransId) {
        try {
            //获取用户的语言
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isEmpty(lang)) {
                lang = "en-US";
            }
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            tonManageService.handleTronTransAccount(tronTransId, userInfoResult,lang);
            return R.ok();
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            log.error(String.format("付款失败,tronTransId=%s,具体失败信息:", tronTransId), e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "付款失败,请联系管理员!");
        }
    }*/

    @NoRepeatSubmit()
    @RequestMapping("/applyTronWithdrawMoney")
    public ResponseEntity<?> applyTronWithdrawMoney(HttpServletRequest httpRequest, @RequestBody @Valid TronWithdrawMoneyVO tronWithdrawMoneyVO) {
        try {
            //获取用户的语言
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isEmpty(lang)) {
                lang = "en-US";
            }
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            return tonManageService.applyTronWithdrawMoney(tronWithdrawMoneyVO, userInfoResult,lang);
        } catch (Exception e) {
            log.error(String.format("申请提现失败,tronWithdrawMoneyVO=%s,具体失败信息:", JSONUtil.toJsonStr(tronWithdrawMoneyVO)), e);
            return ResponseEntity.failure(HttpStatus.SC_INTERNAL_SERVER_ERROR, "申请提现失败,请联系管理员!");
        }
    }

    /**
     * 用户购买会员
     *
     * @param httpRequest
     * @param userBuyPremiumVO
     * @return
     */
    @NoRepeatSubmit()
    @RequestMapping("/handleUserBuyPremium")
    public ResponseEntity<?> handleUserBuyPremium(HttpServletRequest httpRequest, @RequestBody UserBuyPremiumVO userBuyPremiumVO) {
        try {
            //获取用户的语言
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isEmpty(lang)) {
                lang = "en-US";
            }
            /**
             * 1、从header中获取token和chargepsw
             */
            String token = checkTokenUtil.getRequestToken(httpRequest);
            //String chargePassword = checkChargePswUtil.getRequestChargePsw(httpRequest);

            /**
             * 2、根据token获取用户相关信息
             */
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);

            /**
             * 3、根据tgid判断用户是否注册到小程序
             */
            TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
            if (tUser == null) {
                log.warn("TronCtrlController.handleUserBuyPremium 用户不存在,userInfoResult={}", JSONUtil.toJsonStr(userInfoResult));
                //return R.error(ResultCodeEnum.USER_IS_NOT_EXIST.code, I18nUtil.getMessage("4002", lang));
                return ResponseEntity.failure(ResultCodeEnum.USER_IS_NOT_EXIST.code, I18nUtil.getMessage("4002", lang));
            }

            /**
             * 4、判断支付密码是否有效
             */
            /*if (!checkChargePswUtil.verifyChargePsw(chargePassword, tUser.getChargePsw())) {
                log.warn("TronCtrlController.handleUserBuyPremium 验证支付密码失败,支付密码错误,userInfoResult={}", JSONUtil.toJsonStr(userInfoResult));
                //return R.error(ResultCodeEnum.USER_PSW_ERROR.code, I18nUtil.getMessage("6005", lang));
                return ResponseEntity.failure(ResultCodeEnum.USER_PSW_ERROR.code, I18nUtil.getMessage("6005", lang));
            }*/

            /**
             * 5、会员购买 0//手购买会员  1，自动购买会员
             */
            if ("0".equals(myCommonConfig.getCommonPremiumType())) {
                ResponseEntity<?> responseEntity = tonManageService.handleUserBuyPremiumBySelf(userBuyPremiumVO, userInfoResult, lang);
                if (0 != responseEntity.getCode()) {
                    return responseEntity;
                }
            } else {
                ResponseEntity<?> responseEntity = tonManageService.handleUserBuyPremiumByAuto(userBuyPremiumVO, userInfoResult,lang);
                if (0 != responseEntity.getCode()) {
                    return responseEntity;
                }
            }
            //return R.ok();
            return ResponseEntity.success("成功");
        } catch (Exception e) {
            log.error(String.format("用户购买会员失败,userBuyPremiumVO=%s,具体失败信息:", JSONUtil.toJsonStr(userBuyPremiumVO)), e);
            //return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "用户购买会员失败,请联系管理员!");
            return ResponseEntity.failure(HttpStatus.SC_INTERNAL_SERVER_ERROR, "购买会员失败,请联系管理员!");
        }
    }



    /**
     * 获取收款订单的信息
     *
     * @param httpRequest
     * @param tronTransId
     * @return
     */
    /*@NoRepeatSubmit()
    @RequestMapping("/getTronTransAccount")*/
    public R getTronTransAccount(HttpServletRequest httpRequest, String tronTransId) {
        //获取用户的语言
        String lang = httpRequest.getHeader("lang");
        if (StringUtils.isEmpty(lang)) {
            lang = "en-US";
        }
        try {

            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            TronTransResult tronTransResult = tonManageService.getTronTransAccount(tronTransId, userInfoResult, lang);
            return R.ok().put("result", tronTransResult);
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            log.error(String.format("付款失败,tronTransId=%s,具体失败信息:", tronTransId), e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, I18nUtil.getMessage("607",lang));
        }
    }


    /**
     * 付款订单信息
     *
     * @param httpRequest
     * @param tronTransId
     * @return
     */
    /*@NoRepeatSubmit()
    @RequestMapping("/handleUserPayment")*/
    public R handleUserPayment(HttpServletRequest httpRequest, String tronTransId,String shareUserId) {
        // 获取用户选择的语言 cn/en
        String lang = httpRequest.getHeader("lang");
        if (StringUtils.isEmpty(lang)) {
            lang = "en-US";
        }
        try {
            /**
             * 根据token获取用户信息
             */
            String token = checkTokenUtil.getRequestToken(httpRequest);
            String chargePassword = checkChargePswUtil.getRequestChargePsw(httpRequest);

            /**
             * 2、根据token获取用户相关信息
             */
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);

            /**
             * 3、根据tgid判断用户是否注册到小程序
             */
            TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
            if (tUser == null) {
                log.warn("TronCtrlController.handleUserPayment 用户不存在,userInfoResult={}", JSONUtil.toJsonStr(userInfoResult));
                return R.error(ResultCodeEnum.USER_IS_NOT_EXIST.code, I18nUtil.getMessage("4002", lang));
            }

            /**
             * 4、判断支付密码是否有效
             */
            if (!checkChargePswUtil.verifyChargePsw(chargePassword, tUser.getChargePsw())) {
                log.warn("TronCtrlController.handleUserPayment 验证支付密码失败,支付密码错误,userInfoResult={}", JSONUtil.toJsonStr(userInfoResult));
                return R.error(ResultCodeEnum.USER_PSW_ERROR.code, I18nUtil.getMessage("6005", lang));
            }

            return tonManageService.handleUserPayment(tronTransId, userInfoResult.getUserTGID(), lang,shareUserId);
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            log.error(String.format("处理用户付款失败,tronTransId=%s,具体失败信息:", tronTransId), e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, I18nUtil.getMessage("607",lang));
        }
    }


}
