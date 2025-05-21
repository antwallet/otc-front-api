package cn.com.otc.monitor.service;

import cn.com.otc.monitor.entity.po.SysOperLogPO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 日志配置表
 *
 * @author zhangliyan
 * @date 2022-05-25 11:23:16
 */
public interface SysOperLogService extends IService<SysOperLogPO> {
     void insertSysOperLog(SysOperLogPO po);
     void cleanOperLog();
}

