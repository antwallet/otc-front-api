package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.utils.CheckTokenUtil;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.result.TronTransRequest;
import cn.com.otc.modular.sys.service.TTronTransService;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @description: 收款控制类
 * @author: zhangliyan
 * @time: 2024/3/4
 */
@Slf4j
@RestController
@RequestMapping("/api/front/tronTrans")
public class TTronTransController {

    @Autowired
    private TTronTransService tTronTransService;

    @Autowired
    private CheckTokenUtil checkTokenUtil;



    /**
     * 功能描述: 查看用户收款的记录
     *
     * @auther: 2024
     * @date: 2024/7/8 下午4:00
     */
    /*@NoRepeatSubmit()
    @RequestMapping("/list")*/
    public R list(HttpServletRequest httpRequest) {
        try {
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isBlank(lang)) {
                lang = "en-US";
            }
            String token = checkTokenUtil.getRequestToken(httpRequest);
            UserInfoResult userInfoResult = checkTokenUtil.getUserInfoByToken(token);
            return R.ok().put("list",tTronTransService.tTronTransList(userInfoResult,lang));
        } catch (Exception e) {
            log.error("更新是否展示弹窗失败,具体失败信息:", e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            return R.error();
        }
    }




    /**
     * 功能描述: 处理停止收款操作
     *
     * @auther: 2024
     * @date: 2024/7/8 下午4:00
     */
    /*@NoRepeatSubmit()
    @RequestMapping("/handleStopPayment")*/
    public R handleStopPayment(HttpServletRequest httpRequest, @RequestParam("id") String id) {
        try {
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isBlank(lang)) {
                lang = "en-US";
            }
            tTronTransService.handleStopPayment(id,lang);
            return R.ok();
        } catch (Exception e) {
            log.error("更新是否展示弹窗失败,具体失败信息:", e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            return R.error();
        }
    }

}
