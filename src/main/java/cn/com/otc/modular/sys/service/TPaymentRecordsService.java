package cn.com.otc.modular.sys.service;

import cn.com.otc.modular.sys.bean.result.TPaymentRecordsResult;
import cn.com.otc.modular.sys.bean.pojo.TPaymentRecords;
import cn.com.otc.modular.sys.bean.vo.TPaymentRecordsVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 付款记录表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-03-26
 */
public interface TPaymentRecordsService extends IService<TPaymentRecords> {

        TPaymentRecords getLatestPaymentRecord(String tronTransId, String paymentUserId);

        void saveTPaymentRecords(String tronTransId, String paymentId, String sendUserId, String userId, String paymentMoney,
                                 Integer accountType, long durationInHours, String chatName, Integer status, String sharingRatio, String shareUserId);

        void modifyTPaymentRecordsStatus(Long id, Integer status);

        TPaymentRecordsResult tPaymentRecordsList(TPaymentRecordsVo tPaymentRecordsVo, String lang);

        List<TPaymentRecords> getTPaymentRecordsList();

        List<TPaymentRecords> selectTPaymentRecords(List<String> tronTransId, String userTGID);

        //处理订阅到期的用户
        //void handleExpiredSubscriptions();


        //处理续费的用户
        void modifyTPaymentRecordsStatusAndSubscriptionExpiryTime(Long id, Integer paymentStatus5);

        // 根据paymentId查询付款记录
        String getPaymentRecords(String paymentId);


        /**
         * 处理订阅快过期时，提前48小时提醒用户
         */
        //void handleUserBeforeSubscriptionExpires();
}
