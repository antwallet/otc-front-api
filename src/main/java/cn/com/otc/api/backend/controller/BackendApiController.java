package cn.com.otc.api.backend.controller;

import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.redis.NoRepeatSubmit;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.sys.bean.pojo.TTronCollectRecord;
import cn.com.otc.modular.tron.dto.bean.result.CollectTronResult;
import cn.com.otc.modular.tron.service.TronManageService;
import com.alibaba.excel.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description:后台接口
 * @author: zhangliyan
 * @time: 2024/7/26
 */
@Slf4j
@RestController
@RequestMapping("/api/backend/api")
public class BackendApiController {

    @Resource
    private TronManageService tronManageService;

    /**
     * 一键归集区块链余额
     *
     * @return
     */
    @NoRepeatSubmit()
    @RequestMapping("/handleCollectTronAmount")
    public R handleCollectTronAmount() {
        try {
            tronManageService.handleCollectTronAmount();
            return R.ok();
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            log.error("BackendApiController.handleCollectTronAmount 一键归集区块链余额失败,具体失败信息:", e);
            return R.error();
        }
    }


    /**
     * 刷新归集的状态
     *
     * @return
     */
    @NoRepeatSubmit()
    @RequestMapping("/handleRefreshCollectTronStatus")
    public R handleRefreshCollectTronStatus(HttpServletRequest httpRequest, String taskId, String collectTime) {
        try {
            //获取用户的语言
            String lang = httpRequest.getHeader("lang");
            if (StringUtils.isEmpty(lang)) {
                lang = "en-US";
            }
            tronManageService.handleRefreshCollectTronStatus(taskId,collectTime,lang);
            return R.ok();
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            log.error("BackendApiController.handleRefreshCollectTronStatus 刷新归集的状态失败,具体失败信息:", e);
            return R.error();
        }
    }


    /**
     * 获取归集记录
     *
     * @return
     */
    @NoRepeatSubmit()
    @RequestMapping("/getCollectTronResultList")
    public R getCollectTronResultList() {
        try {
            List<CollectTronResult> list = tronManageService.getCollectTronResultList();
            return R.ok().put("list",list);
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            log.error("BackendApiController.getCollectTronResultList 获取归集记录失败,具体失败信息:", e);
            return R.error();
        }
    }


    /**
     * 获取归集详细记录
     *
     * @return
     */
    @NoRepeatSubmit()
    @RequestMapping("/getCollectTronRecordDetails")
    public R getCollectTronRecordDetails(String taskId,HttpServletRequest httpRequest) {
        try {
            List<TTronCollectRecord> list = tronManageService.getCollectTronRecordDetails(taskId,httpRequest);
            return R.ok().put("list",list);
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            log.error("BackendApiController.getCollectTronRecordDetails 获取归集详细记录失败,具体失败信息:", e);
            return R.error();
        }
    }

    /**
     * 自动提现功能
     *
     * @return
     */
    @NoRepeatSubmit()
    @RequestMapping("/handleWithdrawAuto")
    public R handleWithdrawAuto(Long id,String toAddress,String blockchainType,String withdrawType, String withdrawMoney, HttpServletRequest httpRequest)  {
        try {
            tronManageService.handleWithdrawAuto(id,toAddress, blockchainType,withdrawType,withdrawMoney,httpRequest);
            return R.ok();
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            log.error(String.format("BackendApiController.handleWithdrawAuto ,id=%s,toAddress=%s,blockchainType=%s,withdrawType=%s,withdrawMoney=%s"
                + " 自动提现功能失败,具体失败信息:",id,toAddress,blockchainType,withdrawType,withdrawMoney), e);
            return R.error();
        }
    }

    /**
     *刷新自动提现的状态
     */
    @NoRepeatSubmit()
    @RequestMapping("/refreshWithdrawStatus")
    public R refreshWithdrawStatus(Long id, HttpServletRequest httpRequest) {
        try {
            tronManageService.refreshWithdrawStatus(id,httpRequest);
            return R.ok();
        } catch (Exception e) {
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            log.error(String.format("BackendApiController.refreshWithdrawStatus 刷新自动提现状态失败 ,id=%s,具体失败信息:",id), e);
            return R.error();
        }
    }



}
