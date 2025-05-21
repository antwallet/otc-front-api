package cn.com.otc.modular.sys.dao;

import cn.com.otc.modular.sys.bean.pojo.TTronCollectRecord;
import cn.com.otc.modular.tron.dto.bean.result.CollectTronResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * tron链上金额归集记录表 Mapper 接口
 * </p>
 *
 * @author zhangliyan
 * @since 2024-08-29
 */
public interface TTronCollectRecordDao extends BaseMapper<TTronCollectRecord> {

        List<CollectTronResult> queryTTronCollectRecordGroupByTaskId();

        List<TTronCollectRecord> getTTronCollectRecordListByTaskId(String taskId);

}
