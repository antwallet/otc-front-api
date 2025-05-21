package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.exception.RRException;

import cn.com.otc.common.utils.R;
import cn.com.otc.modular.sys.bean.vo.TPaymentRecordsVo;
import cn.com.otc.modular.sys.service.TPaymentRecordsService;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/4
 */
@Slf4j
@RestController
@RequestMapping("/api/front/tPaymentRecords")
public class TPaymentRecordsController {

    @Autowired
    private TPaymentRecordsService tPaymentRecordsService;



    /**
     * 功能描述: 查看付款用户的记录
     *
     * @auther: 2024
     * @date: 2024/7/8 下午4:00
     */
    /*@NoRepeatSubmit()
    @RequestMapping("/list")*/
    public R list(HttpServletRequest httpRequest, @Valid @RequestBody TPaymentRecordsVo tPaymentRecordsVo) {
        try {
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isBlank(lang)) {
                lang = "en-US";
            }
            return R.ok().put("list",tPaymentRecordsService.tPaymentRecordsList(tPaymentRecordsVo,lang));
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
