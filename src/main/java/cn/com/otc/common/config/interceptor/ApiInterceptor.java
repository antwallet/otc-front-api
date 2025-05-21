package cn.com.otc.common.config.interceptor;

import cn.com.otc.common.utils.HttpContextUtils;
import cn.com.otc.common.utils.R;
import com.google.gson.Gson;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class ApiInterceptor implements HandlerInterceptor {
    /**
     * Controller处理前
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param handler 处理对象
     * @return true or false
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       /* String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7);
            // 验证token
            ClientTokenModel token = SaOAuth2Util.checkClientToken(accessToken);

            if (token == null) {
                backResult(response,HttpServletResponse.SC_UNAUTHORIZED,"Invalid token.");
                return false;
            }
        } else {
            backResult(response,HttpServletResponse.SC_BAD_REQUEST,"Missing or invalid Authorization header.");
            return false;
        }*/
        return true;
    }

    private void backResult(HttpServletResponse httpResponse,int code,String msg ){
        try{
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Allow-Origin", HttpContextUtils.getOrigin());

            String json = new Gson().toJson(R.error(code, msg));

            httpResponse.getWriter().print(json);
        }catch (Exception e){
            log.error(String.format("返回结果失败,code=%s,msg=%s,具体失败信息:",code,msg),e);
        }
    }
}
