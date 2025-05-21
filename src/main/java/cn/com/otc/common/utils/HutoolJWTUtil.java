package cn.com.otc.common.utils;

import cn.hutool.core.net.NetUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HutoolJWTUtil {

    //使用HS256签名算法和生成的signingKey最终的Token,claims中是有效载荷
    public  String createTokenHut(Map<String, Object> claims,long TTLMillis,String secret_key) {
        long exp = System.currentTimeMillis()+TTLMillis;
        claims.put("exp", exp/1000); // 秒
        return JWTUtil.createToken(claims, secret_key.getBytes());
    }

    //解析Token，同时也能验证Token，当验证失败返回null
    public  boolean verifyTokenHut(String token,String secret_key) {
        long now = System.currentTimeMillis()/1000;
        try{
            boolean isverify = JWTUtil.verify(token, secret_key.getBytes());
            if(!isverify){
                return false;
            }
            JWT jwt = JWTUtil.parseToken(token);
            if(jwt.getPayload("exp") ==null || (Integer)jwt.getPayload("exp") ==null
                || (Integer)jwt.getPayload("exp") == 0){
                return false;
            }
            long exp = (Integer)jwt.getPayload("exp");
            return now <= exp;
        }catch (Exception e){
            log.error(String.format("token失效,token={%s},具体异常信息",token),e);
            return false;
        }
    }

    //解析Token，同时也能验证Token，当验证失败返回null,这里不验证失效时间
    public  boolean verifyTokenHutNotExp(String token,String secret_key) {
        try{
            return JWTUtil.verify(token, secret_key.getBytes());
        }catch (Exception e){
            log.error(String.format("token失效,token={%s},具体异常信息",token),e);
            return false;
        }
    }

    public static void main(String[] args) {
        HutoolJWTUtil util = new HutoolJWTUtil();
        Map<String,Object> m = new HashMap<String,Object>();
        m.put("ip", NetUtil.getLocalhostStr());
        m.put("userTGID","6758375895");
        m.put("firstName","艾米");
        m.put("userName","tg_aimi_123");
        //设置2小时后失效
        String token = util.createTokenHut(m,7200000,"Sqoh8I53YCBK9d64msJrn6gl5bZ1PU3X");
        System.out.println(token);
        
	}
    
}