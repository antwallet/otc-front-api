package cn.com.otc.common.utils;


import cn.hutool.crypto.digest.BCrypt;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:加密
 * @author: zhangliyan
 * @time: 2021/10/22
 */
@Slf4j
public class EncryptUtil {
    //Java - BCrypt 加密
    public static String encryptByBCrypt(String sKey){
        String salt = BCrypt.gensalt(11);
        String hash =BCrypt.hashpw(sKey, salt);
        return hash;
    }

    //校验密码
    public static boolean checkPswByBCrypt(String passward,String encryptPassward){
        return BCrypt.checkpw(passward, encryptPassward );
    }

    // 测试主函数
    public static void main(String args[]) throws Exception {
        String password = "920605";
        String encryptPassward = encryptByBCrypt(password);
        System.out.println(encryptPassward);
        System.out.println(checkPswByBCrypt(password,encryptPassward));
    }
}
