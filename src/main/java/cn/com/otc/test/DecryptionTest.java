package cn.com.otc.test;

import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @Auther: 2024
 * @Date: 2024/6/25 20:01
 * @Description: 加密解密测试
 */public class DecryptionTest {
    public static final String IV = "94E113C6A898CD39";

    public static final String KEY = "94E113C6A898CD39";

    /*******************************************************************
     * AES加密算法
     * @author moyun
     * 加密用的Key 可以用26个字母和数字组成，最好不要用保留字符，虽然不会错，至于怎么裁决，个人看情况而定    此处使用AES-128-CBC加密模式，key需要为16位。
     * */

    //加密
    public static String Encrypt(String sSrc, String sKey) throws Exception {

        if (sKey == null) {
            System.out.print("Key为空null");
            return null;
        }
        // 判断Key是否为16位

        byte[] raw = sKey.getBytes();

        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes());

        return new String(Base64.encodeBase64(encrypted));//此处使用BAES64做转码功能，同时能起到2次
    }

    //解密
    public static String Decrypt(String sSrc, String sKey) throws Exception {

        // 判断Key是否正确
        if (sKey == null) {
            System.out.print("Key为空null");
            return sSrc;
        }

        if (KEY.length()!=16){
            System.out.print("Key长度不为16位");
            return sSrc;
        }
        byte[] raw = sKey.getBytes("ASCII");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        byte[] encrypted1 = Base64.decodeBase64(sSrc.getBytes());//先用bAES64解密
        try {
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            return originalString;
        } catch (Exception e) {
//            log.info(e.toString());
            return sSrc;
        }
    }

    public static void main(String[] args) throws Exception {

        String pwd = "123";
        String epwd = Encrypt(pwd, "94E113C6A898CD39");
        System.out.println(epwd);
        System.out.println(Decrypt(epwd, "94E113C6A898CD39"));
    }
}
