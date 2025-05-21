package cn.com.otc.common.utils;

import cn.com.otc.common.exception.RRException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author: zhangliyan
 * @Date: 2024/03/06 13:51
 */
@Slf4j
@Component
public class HttpRequestUtil {

    private static final String REQ_SIGN_HEAD_TOKEN = "token";
    private static final int TIME_OUT = 8000;

    public String doRequest (String url,String method, String token,Map<String, Object> paramMap, int timeout) {
        HttpResponse httpResponse = null;
        try {
            if (method.toLowerCase().equals("get")) {
                httpResponse = HttpRequest.get(url)
                        .header(REQ_SIGN_HEAD_TOKEN, token)
                        .keepAlive(true)
                        .timeout(timeout)
                        .form(paramMap)
                        .execute();
            } else {
                String reqBodyStr = JSONUtil.toJsonStr(paramMap);
                httpResponse = HttpRequest.post(url)
                    .header(REQ_SIGN_HEAD_TOKEN, token)
                        .keepAlive(true)
                        .timeout(timeout)
                        .body(reqBodyStr)
                        .execute();
            }
            String body = httpResponse.body();
            if (!httpResponse.isOk()) {
                throw new RRException(String.format("doRequest statusCode error, url=[%s], method=[%s],token =[%s], statusCode=[%s], resBody=[%s]",
                        url, method, token, httpResponse.getStatus(), body));
            }
            return body;

        } catch (Exception e) {
            throw new RRException(String.format("doRequest url=[%s], method=[%s],token =[%s], paramMap=[%s]",
                url, method, token,JSONUtil.toJsonStr(paramMap)), e);
        }finally {
            try {
                // 处理响应
                httpResponse.close();// 确保关闭连接
            }catch (Exception e){

            }

        }
    }
    public String doGetRequest (String url,String token,Map<String, Object> paramMap) {
        return doGetRequest(url,token,paramMap,TIME_OUT);
    }

    public String doGetRequest (String url,String token, Map<String, Object> paramMap, int timeout) {
        return this.doRequest(url,"get", token, paramMap, timeout);
    }
    public String doGetRequest (String url,String token, Map<String, Object> paramMap, int timeout,String lang) {
        return this.doRequest(url,"get", token, paramMap, timeout);
    }

    public String doPostRequest (String url,String token, Map<String, Object> paramMap) {
    	return doPostRequest(url,token, paramMap, TIME_OUT);
    }

    public String doPostRequest (String url,String token, Map<String, Object> paramMap, int timeout) {
        return this.doRequest(url,"post", token, paramMap, timeout);
    }

    public String doPost(String url, String contentType,Map<String,Object> body) {
        String respStr = HttpRequest.post(url)
                .header("Content-Type", contentType)    //消息头，可多个
                .body(JSONUtil.toJsonStr(body))     //接收String类型数据
                .timeout(5000)
                .execute()
                .body();
        return respStr;
    }
}
