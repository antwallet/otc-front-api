package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.utils.CheckTokenUtil;
import cn.com.otc.common.utils.HttpStatus;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TAntwalletConfig;
import cn.com.otc.modular.sys.bean.pojo.TInvitedUser;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.bean.pojo.TUserWithdrawalTj;
import cn.com.otc.modular.sys.bean.result.TUserWithdrawalTjResult;
import cn.com.otc.modular.sys.service.TAntwalletConfigService;
import cn.com.otc.modular.sys.service.TInvitedUserService;
import cn.com.otc.modular.sys.service.TUserService;
import cn.com.otc.modular.sys.service.TUserWithdrawalTjService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/4/12
 */
@Slf4j
@RestController
@RequestMapping("/api/front/userWithdrawalTj")
public class TUserWithdrawalTjController {
    @Autowired
    private TUserWithdrawalTjService tUserWithdrawalTjService;
    @Autowired
    private TUserService tUserService;
    @Autowired
    private TAntwalletConfigService tAntwalletConfigService;
    @Autowired
    private TInvitedUserService tInvitedUserService;
    @Autowired
    private CheckTokenUtil checkTokenUtil;

    private static final String INVITED_SHARE_RATE = "INVITED_SHARE_RATE";

    /**
     * 获取用户提现手续费统计
     *
     * @return
     */
    @RequestMapping("/list")
    public R list(HttpServletRequest httpRequest) {
        try {
            /**
             * 1、根据token获取用户信息
             */
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);

            /**
             * 2、获取用户信息
             */
            TUser tUser = tUserService.getTUserByTGId(userInfoResult.getUserTGID());
            if (tUser == null) {
                return R.error(ResultCodeEnum.USER_IS_NOT_EXIST.code, "用户不存在哦,请选择提供正确的用户哦!");
            }

            TAntwalletConfig tAntwalletConfig = tAntwalletConfigService.getById(INVITED_SHARE_RATE);

            List<TUserWithdrawalTjResult> list = new ArrayList<>();

            TUserWithdrawalTjResult result = new TUserWithdrawalTjResult();
            BigDecimal value = new BigDecimal(tAntwalletConfig.getPValue());
            BigDecimal hundred = new BigDecimal("100");
            BigDecimal percentage = value.multiply(hundred);
            String percentageStr = percentage.toPlainString() + "%";
            result.setLanhutext0(percentageStr);
            result.setLanhutext1("手续费比例");
            list.add(result);

            List<TInvitedUser> tInvitedUserList = tInvitedUserService.getTInvitedUserList(tUser.getUserId());
            TUserWithdrawalTjResult result1 = new TUserWithdrawalTjResult();
            result1.setLanhutext0(tInvitedUserList == null ? "0" : tInvitedUserList.size() + "");
            result1.setLanhutext1("邀请人数");
            list.add(result1);

            TUserWithdrawalTj tUserWithdrawalTj = tUserWithdrawalTjService.getTUserWithdrawalTj(tUser.getUserId());
            if (tUserWithdrawalTj == null) {
                TUserWithdrawalTjResult result2 = new TUserWithdrawalTjResult();
                //result2.setLanhutext0("0 TRX<br/>0 USDT");
                result2.setLanhutext0("0 USDT");
                result2.setLanhutext1("总计手续费");
                TUserWithdrawalTjResult result3 = new TUserWithdrawalTjResult();
                //result3.setLanhutext0("0 TRX<br/>0 USDT");
                result3.setLanhutext0("0 USDT");
                result3.setLanhutext1("获得奖励");
                list.add(result2);
                list.add(result3);
            } else {
                TUserWithdrawalTjResult result2 = new TUserWithdrawalTjResult();
                //result2.setLanhutext0(tUserWithdrawalTj.getWithdrawalTotalMoney() + " TRX<br/>" + tUserWithdrawalTj.getWithdrawalTotalMoneyUsdt() + " USDT");
                result2.setLanhutext0(tUserWithdrawalTj.getWithdrawalTotalMoneyUsdt() + " USDT");
                result2.setLanhutext1("总计手续费");
                TUserWithdrawalTjResult result3 = new TUserWithdrawalTjResult();
                //result3.setLanhutext0(tUserWithdrawalTj.getWithdrawalShareMoney() + " TRX<br/>" + tUserWithdrawalTj.getWithdrawalShareMoneyUsdt() + " USDT");
                result3.setLanhutext0(tUserWithdrawalTj.getWithdrawalShareMoneyUsdt() + " USDT");
                result3.setLanhutext1("获得奖励");
                list.add(result2);
                list.add(result3);
            }
            return R.ok().put("list", list);
        } catch (Exception e) {
            log.error("获取用户提现手续费统计失败,具体失败信息:", e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "获取用户提现手续费统计失败,请联系管理员!");
        }
    }

}
