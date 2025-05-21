package cn.com.otc.monitor.dao;

import cn.com.otc.monitor.entity.po.SysOperLogPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日志表 数据层
 * 
 * @author zhangliyan
 */
@Mapper
public interface SysOperLogDao extends BaseMapper<SysOperLogPO>
{
   void cleanOperLog();
}
