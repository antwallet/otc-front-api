package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.modular.sys.bean.pojo.TTronCollectRecord;
import cn.com.otc.modular.sys.dao.TTronCollectRecordDao;
import cn.com.otc.modular.sys.service.TTronCollectRecordService;
import cn.com.otc.modular.tron.dto.bean.result.CollectTronResult;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @description: tron链上金额归集表 服务层
 * @author: zhangliyan
 * @time: 2024/8/29
 */
@Service
public class TTronCollectServiceImpl extends ServiceImpl<TTronCollectRecordDao, TTronCollectRecord> implements
        TTronCollectRecordService {

  @Override
  public List<TTronCollectRecord> getTTronCollectRecordListByTaskId(String taskId) {

    return this.baseMapper.getTTronCollectRecordListByTaskId(taskId);
  }

  @Override
  public List<CollectTronResult> getTTronCollectRecordGroupByTaskId() {
    return this.baseMapper.queryTTronCollectRecordGroupByTaskId();
  }

  @Override
  public void saveTTronCollectRecord(String fromAddress, String toAddress, String tronType, String collectMoney,
                                     String taskId, String collectTime, Integer status, String desc,String collectTxid) {
    TTronCollectRecord tTronCollectRecord = new TTronCollectRecord();
    tTronCollectRecord.setFromAddress(fromAddress);
    tTronCollectRecord.setToAddress(toAddress);
    tTronCollectRecord.setTronType(tronType);
    tTronCollectRecord.setCollectMoney(collectMoney);
    tTronCollectRecord.setTaskId(taskId);
    tTronCollectRecord.setCollectTime(collectTime);
    tTronCollectRecord.setStatus(status);
    tTronCollectRecord.setDesc(desc);
    tTronCollectRecord.setCollectTxid(collectTxid);
    tTronCollectRecord.setCreateTime(LocalDateTime.now());
    this.save(tTronCollectRecord);
  }

  @Override
  public void modifyTTronCollectRecord(Long id, Integer status, String desc,Integer isrefresh) {
    TTronCollectRecord tTronCollectRecord = new TTronCollectRecord();
    tTronCollectRecord.setId(id);
    tTronCollectRecord.setStatus(status);
    tTronCollectRecord.setDesc(desc);
    tTronCollectRecord.setIsrefresh(isrefresh);
    tTronCollectRecord.setUpdateTime(LocalDateTime.now());
    this.updateById(tTronCollectRecord);
  }

}
