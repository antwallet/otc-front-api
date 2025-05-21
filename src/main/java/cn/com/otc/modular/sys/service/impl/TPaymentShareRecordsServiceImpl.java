package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.modular.sys.bean.pojo.TPaymentShareRecords;
import cn.com.otc.modular.sys.dao.TPaymentShareRecordsDao;
import cn.com.otc.modular.sys.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/26
 */
@Service
public class TPaymentShareRecordsServiceImpl extends ServiceImpl<TPaymentShareRecordsDao, TPaymentShareRecords> implements
        TPaymentShareRecordsService {
    @Override
    @Async
    public void saveTPaymentShareRecords(String tronTransId, String paymentId, String shareUserId, String invitedUserId) {
        TPaymentShareRecords tPaymentShareRecords = new TPaymentShareRecords();
        LocalDateTime now = LocalDateTime.now();
        tPaymentShareRecords.setTronTransId(tronTransId);
        tPaymentShareRecords.setPaymentId(paymentId);
        tPaymentShareRecords.setShareUserId(shareUserId);
        tPaymentShareRecords.setInvitedUserId(invitedUserId);
        tPaymentShareRecords.setCreateTime(now);
        this.save(tPaymentShareRecords);
    }

}
