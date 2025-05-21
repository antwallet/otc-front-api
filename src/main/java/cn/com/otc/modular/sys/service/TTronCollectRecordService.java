package cn.com.otc.modular.sys.service;

import cn.com.otc.modular.sys.bean.pojo.TTronCollectRecord;
import cn.com.otc.modular.tron.dto.bean.result.CollectTronResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * tron链上金额归集表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-08-29
 */
public interface TTronCollectRecordService extends IService<TTronCollectRecord> {
        List<TTronCollectRecord> getTTronCollectRecordListByTaskId(String taskId);

        List<CollectTronResult>  getTTronCollectRecordGroupByTaskId();

        void saveTTronCollectRecord(String fromAddress,String toAddress,String tronType,String collectMoney,
                                    String taskId,String collectTime,Integer status,String desc,String collectTxid);

        void modifyTTronCollectRecord(Long id,Integer status,String desc,Integer isrefresh);

}
