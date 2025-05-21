package cn.com.otc.modular.tron.util;

import io.netty.handler.codec.http.HttpMethod;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * 签名demo
 */
@Component
public class MeFreeUtil {

    /**
     * 创建签名
     * MF-ACCESS-SIGN的请求头是对timestamp + method + requestPath + body字符串（+表示字符串连接），以及SecretKey，使用HMAC SHA256方法加密，通过Base-64编码输出而得到的。
     * 如：sign=CryptoJS.enc.Base64.stringify(CryptoJS.HmacSHA256(timestamp + 'GET' + '/api/config?p1=value', SecretKey))
     * 其中，timestamp的值与MF-ACCESS-TIMESTAMP请求头相同，为ISO格式，如2020-12-08T09:08:57.715Z。
     * method是请求方法，字母全部大写：GET/POST。
     * requestPath是请求接口路径,包含query参数。如：/api/config?p1=value1
     * body是指请求主体的字符串，如果请求没有主体（通常为GET请求）则body可省略。如：{"p1":"v1"}
     *
     * @param value timestamp（请求时间,格式yyyy-MM-dd'T'HH:mm:ss.SSS'Z'） + method（请求方法GET|POST） + uriPath（请求路径，对应API接口） + uriQuery(请求参数可选)
     *              例如得到账户配置:api/config
     *              value = 2008-08-08T08:08:08.888ZGETapi/config
     *              例如创建订单:api/order
     *              value = 2008-08-08T08:08:08.888ZPOSTapi/order?quantity=32000&period=1&target_address=Txxx...
     * @param key   用户的API_SECRET
     * @return
     */
    public static String createSign(String value, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        mac.update(value.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(mac.doFinal());
    }

    public static void main(String[] args) throws Exception {
        // 替换自己的key和secret
        String API_KEY = "7382805411";
        String API_SECRET = "55f73643a0527d6ba5ca0a3bd5a04ca3";
        String config = config(API_KEY, API_SECRET);
        System.out.println(config);
//        order(API_KEY, API_SECRET,period,quantity,targetAddress);
    }

    public static String config(String API_KEY, String API_SECRET) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
        HttpMethod method = HttpMethod.GET; // get请求
        URI uri = URI.create("https://api.mefree.net/api/config");
        String value = String.format("%s%s%s", timestamp, method, uri.getPath());

        try {
            String sign = createSign(value, API_SECRET);
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://api.mefree.net/api/config")
                    .get()
                    .addHeader("MF-ACCESS-KEY", API_KEY)
                    .addHeader("MF-ACCESS-TIMESTAMP", timestamp)
                    .addHeader("MF-ACCESS-SIGN", sign)
                    .addHeader("Accept", "*/*")
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public static String order(String API_KEY, String API_SECRET, Integer period, Integer quantity, String targetAddress,Integer count) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
//        Integer period = 1;
//        Integer quantity = 32000;
//        String targetAddress = "xxx";
        try {
            HttpMethod method = HttpMethod.POST; // post请求
            String url = String.format("https://api.mefree.net/api/order?period=%d&quantity=%d&target_address=%s&count=%s",period, quantity, targetAddress,count);
            URI uri = URI.create(url);
            String value = String.format("%s%s%s", timestamp, method, uri.getPath());
            // 如果请求后面有参数，需要添加上参数，注意，这里要加上？
            if(uri.getQuery() != null){
                value = value +"?"+ uri.getQuery();
            }
            String sign = createSign(value, API_SECRET);
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .post(new FormBody.Builder().build())
                    .addHeader("MF-ACCESS-KEY", API_KEY)
                    .addHeader("MF-ACCESS-TIMESTAMP", timestamp)
                    .addHeader("MF-ACCESS-SIGN", sign)
                    .addHeader("Accept", "*/*")
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
