package cn.com.otc.modular.sys.service;

import cn.com.otc.modular.sys.bean.pojo.TPaymentShareRecords;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 收款分享记录表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-03-26
 */
public interface TPaymentShareRecordsService extends IService<TPaymentShareRecords> {
        void saveTPaymentShareRecords(String tronTransId, String paymentId, String shareUserId, String invitedUserId);

}
