package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.redis.NoRepeatSubmit;
import cn.com.otc.common.utils.CheckTokenUtil;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TPasswordResetLog;
import cn.com.otc.modular.sys.service.TPasswordResetLogService;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/2/26
 */
@Slf4j
@RestController
@RequestMapping("/api/front/t_password_reset")
public class TPasswordResetLogController {

  @Autowired
  private TPasswordResetLogService tPasswordResetLogService;

  @Autowired
  private CheckTokenUtil checkTokenUtil;


  /*
  * 查看该用户是否有过重置密码的操作
  * */
  @NoRepeatSubmit()
  @RequestMapping("/checkPasswordResetLog")
  public R checkPasswordResetLog(HttpServletRequest httpRequest){
    try{
      String lang = httpRequest.getHeader("lang");
      if (StringUtils.isBlank(lang)) {
        lang = "en-US";
      }

      String token = checkTokenUtil.getRequestToken(httpRequest);
      UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
      TPasswordResetLog tPasswordResetLog = tPasswordResetLogService.checkPasswordResetLog(userInfoResult, lang);
      if (tPasswordResetLog==null){
        return R.error("请先去设置支付密码");
      }
      return R.ok().put("result",tPasswordResetLog);
    }catch (Exception e){
      log.error("修改支付密码失败,具体失败信息:",e);
      if(e instanceof RRException){
        RRException rrException = (RRException)e;
        return R.error(rrException.getCode(),rrException.getMsg());
      }
      return R.error();
    }
  }

}
