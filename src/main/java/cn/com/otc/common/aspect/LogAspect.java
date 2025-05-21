package cn.com.otc.common.aspect;

import cn.com.otc.common.annotation.Log;
import cn.com.otc.common.utils.HttpContextUtils;
import cn.com.otc.common.utils.IPUtils;
import cn.com.otc.monitor.entity.po.SysOperLogPO;
import cn.com.otc.monitor.service.SysOperLogService;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * @description:日志aop切面类
 * @author: zhangliyan
 * @time: 2022/5/20
 */
@Aspect
@Slf4j
@Component
public class LogAspect {

    @Autowired
    private SysOperLogService sysOperLogService;

    /** 排除敏感属性字段 */
    public static final String[] EXCLUDE_PROPERTIES = { "password", "oldPassword", "newPassword", "confirmPassword" };

    @Pointcut("@annotation(cn.com.otc.common.annotation.Log)")
    public void logPointCut() {

    }

    /**
     * 方法请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(logRecord)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log logRecord, Object jsonResult)
    {
        handleLog(joinPoint,logRecord,jsonResult);
    }

    private void handleLog(JoinPoint joinPoint, Log logRecord, Object jsonResult){
        //获取request
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        try{
            String operName = "未知";

            SysOperLogPO operLog = new SysOperLogPO();

            //获取操作人员
            if(StringUtils.isNotBlank(operName)){
                operLog.setOperName(operName);
            }
            //获取IP地址
            operLog.setOperIp(IPUtils.getIpAddr(request));
            operLog.setOperUrl(request.getRequestURI());

            // 获取方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            // 获取请求方式
            operLog.setRequestMethod(request.getMethod());

            //获取日志注解中的值设置到SysOperLogPO中
           if(logRecord !=null){
               operLog.setTitle(logRecord.desc());
               operLog.setBusinessType(logRecord.action().ordinal());
           }

            //获取请求的参数
            operLog.setOperParam(getOperParam(request,joinPoint));

           //获取返回结果
            String result = JSONUtil.toJsonStr(jsonResult);
            operLog.setJsonResult(StringUtils.substring(result,0,2000));

            operLog.setOperTime(new Date());

            sysOperLogService.insertSysOperLog(operLog);
        }catch (Exception ex){
            String errMsg = String.format("[LogAspect.doAfterReturning] 记录日志异常：");
            log.error(errMsg,ex);
        }
    }


    private String getOperParam(HttpServletRequest request,JoinPoint joinPoint) throws Exception{
            Map<String, String[]> map = request.getParameterMap();
            if(map !=null && map.size() >0){
                String params = JSONUtil.toJsonStr(map);
                return StringUtils.substring(params,0,2000);
            }else{
                Object args = joinPoint.getArgs();
                String params = JSONUtil.toJsonStr(args);
                return StringUtils.substring(params,0,2000);
            }
    }

}
