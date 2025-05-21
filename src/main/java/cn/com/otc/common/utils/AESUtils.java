package cn.com.otc.common.utils;

import cn.com.otc.common.exception.RRException;
import cn.hutool.crypto.SecureUtil;
import org.apache.logging.log4j.util.Strings;
import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;


/**
 * @Auther: 2024
 * @Date: 2024/6/25 13:59
 * @Description: AES加密解密工具类
 */
@Component
public class AESUtils {

    private static final String AES_ECB = "AES/ECB/PKCS5Padding";
    private static final Logger log = LoggerFactory.getLogger(AESUtils.class);
    public static final String IV = "94E113C6A898CD39";
    /**
     *
     * 功能描述: 解密方法
     *
     * @auther: 2024
     * @date: 2024/6/25 下午2:20
     */
    public  String decrypt(String encryptedData, String key)  {
        if (Strings.isBlank(key)){
            throw new RRException("key为空啦!");
        }
        if (key.length()!=16){
            throw new RRException("Key长度不为16位");
        }

        try {
            byte[] raw = key.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = Base64.decodeBase64(encryptedData.getBytes());//先用bAES64解密
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            return originalString;
        } catch (Exception e) {
            log.info(e.toString());
            throw new RRException("解密失败啦!"+e);
        }
    }



    //加密 成byte[]
    public static String encrypt(String sSrc) {

        // 使用AES算法进行加密
        byte[] keyBytes = "94E113C6A858CD39".getBytes(StandardCharsets.UTF_8);
        String encryptHex = null;
        try {
            encryptHex = SecureUtil.aes(keyBytes).encryptHex(sSrc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return encryptHex;
    }

    /**
     * 功能描述: 解密方法 byte[]类型的数据
     *
     * @auther: 2024
     * @date: 2024/6/25 下午2:20
     */
    public static String decrypt(String encryptedData) {
        // 解密数据
        byte[] keyBytes = "94E113C6A858CD39".getBytes(StandardCharsets.UTF_8);
        String decryptedText = null;
        try {
            decryptedText = SecureUtil.aes(keyBytes).decryptStr(encryptedData);
            return decryptedText;
        } catch (Exception e) {
            throw new RuntimeException("解密失败了"+e);
        }
    }

}
