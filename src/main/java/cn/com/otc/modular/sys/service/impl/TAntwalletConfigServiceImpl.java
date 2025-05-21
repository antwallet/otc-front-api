package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.modular.sys.bean.pojo.TAntwalletConfig;
import cn.com.otc.modular.sys.dao.TAntwalletConfigDao;
import cn.com.otc.modular.sys.service.TAntwalletConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import org.springframework.stereotype.Service;

/**
 * @description:配置表管理实现类
 * @author: zhangliyan
 * @time: 2024/4/12
 */
@Service
public class TAntwalletConfigServiceImpl extends ServiceImpl<TAntwalletConfigDao, TAntwalletConfig> implements
        TAntwalletConfigService {

    @Override
    public TAntwalletConfig selectTAntwalletbotConfigByPKey(String pKey) {
        LambdaQueryWrapper<TAntwalletConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TAntwalletConfig::getPKey, pKey);
        return this.getOne(queryWrapper);
    }


}
