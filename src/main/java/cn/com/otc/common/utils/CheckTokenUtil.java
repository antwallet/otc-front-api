package cn.com.otc.common.utils;

import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description:校验token
 * @author: zhangliyan
 * @time: 2024/2/21
 */
@Slf4j
@Component
public class CheckTokenUtil {

  @Autowired
  private HutoolJWTUtil hutoolJWTUtil;
  @Autowired
  private MyCommonConfig commonConfig;

  /**
   * 获取请求的token
   */
  public String getRequestToken(HttpServletRequest httpRequest) {
    //从参数中获取token
    String token = httpRequest.getParameter("token");
    //如果参数中不存在token，则从header中获取token
    if (StringUtils.isBlank(token)) {
      token = httpRequest.getHeader("token");
    }
    return token;
  }

  public boolean verifyToken(HttpServletRequest httpRequest) throws Exception{
    String token = getRequestToken(httpRequest);//获取token
    String url = httpRequest.getRequestURI();
    boolean isVerify = false;
    if(StringUtils.isBlank(token)){
      log.warn("访问[{}] token is null",url);
      return isVerify;
    }
    isVerify = hutoolJWTUtil.verifyTokenHutNotExp(token,commonConfig.getCommonTokenSecret());
    if (isVerify) {
      JWT jwt = JWTUtil.parseToken(token);
      if (jwt.getPayload("ip") == null) {
        log.warn("访问[{}] token中携带的ip不存在",url);
        return false;
      }
//      String ip = (String) jwt.getPayload("ip");
//      if (!NetUtil.getLocalhostStr().equals(ip)) {
//        log.warn("访问[{}] token中携带的ip和当前的ip不一致",url);
//        return false;
//      }

      if (jwt.getPayload("userTGID") == null) {
        log.warn("访问[{}] token中携带的userTGID不存在",url);
        return false;
      }

      if (jwt.getPayload("firstName") == null) {
        log.warn("访问[{}] token中携带的firstName不存在",url);
        return false;
      }
    }
    return isVerify;
  }

  public UserInfoResult getUserInfoByToken(String token){
    JWT jwt = JWTUtil.parseToken(token);
    String userTGID = (String) jwt.getPayload("userTGID");
    String firstName = (String) jwt.getPayload("firstName");
    String userName = "";
    if(jwt.getPayload("userName") ==null){
      userName = "未设置";
    }else{
      userName = (String) jwt.getPayload("userName");
    }
    String avatar = (String) jwt.getPayload("avatar");

    UserInfoResult userInfo = new UserInfoResult();
    userInfo.setUserTGID(userTGID);
    userInfo.setFirstName(firstName);
    userInfo.setUserName(userName);
    userInfo.setAvatar(avatar);
    return userInfo;
  }

  public String getRedPacketIdByToken(String token){
    JWT jwt = JWTUtil.parseToken(token);
    if(jwt.getPayload("redpacketId") ==null){
       return null;
    }
    String redpacketId = (String) jwt.getPayload("redpacketId");
    return redpacketId;
  }

}
