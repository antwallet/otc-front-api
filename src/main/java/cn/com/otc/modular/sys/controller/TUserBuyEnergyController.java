package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.utils.CheckTokenUtil;
import cn.com.otc.common.utils.HttpStatus;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.bean.pojo.TUserBuyEnergy;
import cn.com.otc.modular.sys.service.TUserBuyEnergyService;
import cn.com.otc.modular.sys.service.TUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Auther: 2024
 * @Date: 2024/8/8 10:40
 * @Description: 用户购买能量
 */
@Slf4j
@RestController
@RequestMapping("/api/front/energy")
public class TUserBuyEnergyController {

    @Autowired
    private TUserBuyEnergyService tUserBuyEnergyService;

    @Autowired
    private CheckTokenUtil checkTokenUtil;

    @Autowired
    private TUserService tUserService;

    /**
     * 根据token获取用户充值记录
     * @return
     */
    @RequestMapping("/list")
    public R list(HttpServletRequest httpRequest){
        try{

            /**
             * 1、根据token获取用户信息
             */
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);

            /**
             * 2、获取用户信息
             */
            LambdaQueryWrapper<TUser> lambdaQueryWrapper_user = new LambdaQueryWrapper<>();
            lambdaQueryWrapper_user.eq(TUser::getTgId,userInfoResult.getUserTGID());
            lambdaQueryWrapper_user.eq(TUser::getIslock,0);
            TUser tUser = tUserService.getOne(lambdaQueryWrapper_user);
            if(tUser == null){
                return R.error(ResultCodeEnum.USER_IS_NOT_EXIST.code,"用户不存在哦,请选择提供正确的用户哦!");
            }
            List<TUserBuyEnergy> list = tUserBuyEnergyService.list(tUser);
            return R.ok().put("list",list);
        }catch (Exception e){
            log.error("根据token获取用户账户信息失败,具体失败信息:",e);
            return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR,"获取用户账户信息失败,请联系管理员!");
        }
    }


}
