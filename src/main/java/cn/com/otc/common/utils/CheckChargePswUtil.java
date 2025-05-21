package cn.com.otc.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:校验支付密码
 * @author: zhangliyan
 * @time: 2024/7/14
 */
@Slf4j
@Component
public class CheckChargePswUtil {

  @Autowired
  private AESUtils aesUtils;

  /**
   * 获取请求的支付密码
   */
  public String getRequestChargePsw(HttpServletRequest httpRequest) {
    //从参数中获取chargepsw
    String chargepsw = httpRequest.getParameter("chargepsw");
    //如果参数中不存在chargepsw，则从header中获取chargepsw
    if (StringUtils.isBlank(chargepsw)) {
      chargepsw = httpRequest.getHeader("chargepsw");
    }
    return chargepsw;
  }

  public boolean verifyChargePsw(String chargePassword,String userChargePsw) throws Exception{
    boolean isVerify = false;
    if(StringUtils.isBlank(chargePassword)){
      return isVerify;
    }
    String decrypt = aesUtils.decrypt(chargePassword, "94E113C6A898CD39");
    return EncryptUtil.checkPswByBCrypt(decrypt,userChargePsw);
  }

}
