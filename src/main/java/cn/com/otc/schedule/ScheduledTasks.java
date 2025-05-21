package cn.com.otc.schedule;

import cn.com.otc.common.redis.RedisOperate;
import cn.com.otc.modular.sys.service.TPaymentRecordsService;
import cn.com.otc.modular.sys.service.TTronTransService;
import cn.com.otc.modular.tron.service.TronManageService;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @description: 定时任务
 * @author: zhangliyan
 * @time: 2024/3/18
 */
@Slf4j
@Component
public class ScheduledTasks {
    @Resource
    private RedisOperate redisOperate;
    @Autowired
    private TronManageService tronManageService;
    @Autowired
    private TTronTransService tTronTransService;
    @Autowired
    private TPaymentRecordsService tPaymentRecordsService;

    private boolean isChargeStartHandle = true;//
    /**
     * 监控充值交易的情况
     */
    @Scheduled(fixedRate = (1000 * 60))
    public void handleTronChargeRecord() {
        if (!isChargeStartHandle) {
            log.info("ScheduledTasks handleTronChargeRecord 还有没有执行完的任务，等执行完再进行");
            return;
        }
        isChargeStartHandle = false;
        try {
            tronManageService.handleTronCharge();
        } catch (Exception e) {
            log.error("监控交易失败,具体失败信息:", e);
        } finally {
            isChargeStartHandle = true;
        }
    }
    /**
     * USDT和PHP,INR汇率
     * 24小时执行一次
     */
    @Scheduled(fixedRate = 86400000)
    public void handleExchangeRate() {
        log.info("ScheduledTasks-handleExchangeRate 处理USDT和PHP,INR汇率");
        String body = HttpRequest.get("https://api.coingecko.com/api/v3/simple/price?ids=tether&vs_currencies=inr,php").execute().body();
        String inr = JSON.parseObject(body).getJSONObject("tether").getString("inr");
        String php = JSON.parseObject(body).getJSONObject("tether").getString("php");
        redisOperate.setRedisString("INR_PRICE", inr);
        redisOperate.setRedisString("PHP_PRICE", php);
    }

}

