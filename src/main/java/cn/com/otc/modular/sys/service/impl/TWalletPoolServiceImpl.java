package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.modular.sys.dao.TWalletPoolDao;
import cn.com.otc.modular.sys.bean.pojo.TWalletPool;
import cn.com.otc.modular.sys.service.TWalletPoolService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @description:钱包地址池管理实现类
 * @author: zhangliyan
 * @time: 2024/4/1
 */
@Service
public class TWalletPoolServiceImpl extends ServiceImpl<TWalletPoolDao, TWalletPool> implements
    TWalletPoolService {
  @Override
  public List<TWalletPool> getTWalletPoolList() {
    LambdaQueryWrapper<TWalletPool> lambdaQueryWrapper = new LambdaQueryWrapper();
    lambdaQueryWrapper.eq(TWalletPool::getIsdel,0);
    return this.list(lambdaQueryWrapper);
  }

  @Override
  public TWalletPool getTWalletPoolByAddress(String address) {
    LambdaQueryWrapper<TWalletPool> lambdaQueryWrapper = new LambdaQueryWrapper();
    lambdaQueryWrapper.eq(TWalletPool::getBase58CheckAddress,address);
    lambdaQueryWrapper.eq(TWalletPool::getIsdel,0);
    return this.getOne(lambdaQueryWrapper);
  }

}
