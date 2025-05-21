package cn.com.otc.common.utils;

import cn.com.otc.common.config.MyCommonConfig;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/6/11
 */
@Slf4j
@Component
public class CommonApiBaseUtil {
  @Resource
  public MyCommonConfig myCommonConfig;

  public static final String OPS_ACTION_API_AUTHORIZATION = "X-ANTWALLETBOT-OPS-Signature";

  // 获取token
  public static String getOpsSignature(HttpServletRequest request) {
    return request.getHeader(OPS_ACTION_API_AUTHORIZATION);
  }

  public boolean verifySignature(String shushuSignature,String requestBody){
    HMac mac = new HMac(HmacAlgorithm.HmacSHA1, myCommonConfig.getApiSignatureSecret().getBytes());
    String mysignature = mac.digestHex(requestBody);
    if(shushuSignature.equals(mysignature)){
      return true;
    }
    return false;
  }


}
