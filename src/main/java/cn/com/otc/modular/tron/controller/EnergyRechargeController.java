package cn.com.otc.modular.tron.controller;

import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.redis.NoRepeatSubmit;
import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.*;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.service.TUserService;
import cn.com.otc.modular.tron.dto.bean.vo.UserBuyEnergyVO;
import cn.com.otc.modular.tron.service.EnergyRechargeService;
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
import java.util.HashMap;

/**
 * @description: 能量充值控制器
 * @author: zhangliyan
 * @time: 2024/2/26
 */
@Slf4j
@RestController
@RequestMapping("/api/front/energy_recharge")
public class EnergyRechargeController {

    @Autowired
    private EnergyRechargeService energyRechargeService;
    @Autowired
    private CheckTokenUtil checkTokenUtil;
    @Autowired
    private TUserService tUserService;

    @Autowired
    private CheckChargePswUtil checkChargePswUtil;


    /**
     * 用户购买能量
     * @param httpRequest
     * @param userBuyEnergyVO
     * @return
     */
    @NoRepeatSubmit()
    @RequestMapping("/handleUserBuyEnergy")
    public ResponseEntity<?> handleUserBuyEnergy(HttpServletRequest httpRequest, @RequestBody @Valid UserBuyEnergyVO userBuyEnergyVO){
        try{
            //获取用户的语言
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isEmpty(lang)) {
                lang = "en-US";
            }
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            return energyRechargeService.handleUserBuyEnergy(userBuyEnergyVO,userInfoResult,lang);
        }catch (Exception e){
            log.error(String.format("用户购买能量失败,userBuyPremiumVO=%s,具体失败信息:",JSONUtil.toJsonStr(userBuyEnergyVO)),e);
            return ResponseEntity.failure(HttpStatus.SC_INTERNAL_SERVER_ERROR,"用户购买能量失败,请联系管理员!");
        }
    }

    /**
     * 购买金额-trx
     * @param httpRequest
     * @param energyType
     * @param rentTime
     * @return
     */
    @NoRepeatSubmit()
    @RequestMapping("/handlePurchasingPrice/{energyType}/{rentTime}")
    public R handlePurchasingPrice(HttpServletRequest httpRequest, @PathVariable("energyType") String energyType,
                                   @PathVariable("rentTime") String rentTime){
        try{

            //获取用户的语言
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isEmpty(lang)) {
                lang = "en-US";
            }
            /**
             * 1、从header中获取token和chargepsw
             */
            String token = checkTokenUtil.getRequestToken(httpRequest);

            /**
             * 2、根据token获取用户相关信息
             */
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);

            /**
             * 3、根据tgid判断用户是否注册到小程序
             */
            TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
            if(tUser == null){
                log.warn("TronCtrlController.handlePurchasingPrice 用户不存在,userInfoResult={}", JSONUtil.toJsonStr(userInfoResult));
                return R.error(ResultCodeEnum.USER_IS_NOT_EXIST.code, I18nUtil.getMessage("4002",lang));
            }

            String price = energyRechargeService.handlePurchasingPrice(energyType, rentTime, userInfoResult,lang);
            HashMap<String, Object> map = new HashMap<>();
            map.put("price",price);
            return R.ok(map);
        }catch (Exception e){
            if(e instanceof RRException){
                RRException rrException = (RRException)e;
                return R.error(rrException.getCode(),rrException.getMsg());
            }
            log.error(String.format("用户购买能量失败,energyType=%s,rentTime=%s,具体失败信息:",energyType,rentTime),e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR,"用户购买能量失败,请联系管理员!");
        }
    }

}
