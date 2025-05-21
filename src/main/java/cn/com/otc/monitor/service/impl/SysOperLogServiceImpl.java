package cn.com.otc.monitor.service.impl;

import cn.com.otc.monitor.dao.SysOperLogDao;
import cn.com.otc.monitor.entity.po.SysOperLogPO;
import cn.com.otc.monitor.service.SysOperLogService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 日志管理
 */
@Slf4j
@Service("sysOperLogService")
@DS("master")
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogDao, SysOperLogPO> implements SysOperLogService {

    @Async
    @Override
    public void insertSysOperLog(SysOperLogPO po) {
        this.baseMapper.insert(po);
    }

    @Override
    public void cleanOperLog() {
      this.baseMapper.cleanOperLog();
    }
}