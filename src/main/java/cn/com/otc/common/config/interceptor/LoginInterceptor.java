package cn.com.otc.common.config.interceptor;

import cn.com.otc.common.utils.CheckTokenUtil;
import cn.com.otc.common.utils.HttpContextUtils;
import cn.com.otc.common.utils.R;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private CheckTokenUtil checkTokenUtil;
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
        try{
            if (!checkTokenUtil.verifyToken(request)) {
                backResult(response, HttpStatus.SC_UNAUTHORIZED, "invalid token");
                return false;
            }
        }catch (Exception e){
            log.error("请求出现问题,具体失败信息:",e);
            backResult(response,HttpStatus.SC_INTERNAL_SERVER_ERROR,"server error");
            return false;
        }
        return true;
    }

    private void backResult(HttpServletResponse httpResponse,int code,String msg ){
        try{
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Allow-Origin", HttpContextUtils.getOrigin());

            String json = new Gson().toJson(R.error(code, msg));

            httpResponse.getWriter().print(json);
        }catch (Exception e){
            log.error(String.format("返回前端结果失败,code=%s,msg=%s,具体失败信息:",code,msg),e);
        }
    }
}
