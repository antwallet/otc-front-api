package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.modular.sys.bean.pojo.TUserWithdrawalTj;
import cn.com.otc.modular.sys.dao.TUserWithdrawalTjDao;
import cn.com.otc.modular.sys.service.TUserWithdrawalTjService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * @description:用户提现手续费统计管理实现类
 * @author: zhangliyan
 * @time: 2024/4/12
 */
@Service
public class TUserWithdrawalTjServiceImpl extends ServiceImpl<TUserWithdrawalTjDao, TUserWithdrawalTj> implements
    TUserWithdrawalTjService {

  @Override
  public TUserWithdrawalTj getTUserWithdrawalTj(String userId) {
    LambdaQueryWrapper<TUserWithdrawalTj> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.eq(TUserWithdrawalTj::getUserId,userId);
    return this.getOne(lambdaQueryWrapper);
  }

  @Override
  public void saveTUserWithdrawalTj(String userId, String withdrawalTotalMoney,String withdrawalTotalMoneyUsdt,String withdrawalShareMoney,String withdrawalShareMoneyUsdt) {
    TUserWithdrawalTj tUserWithdrawalTj = new TUserWithdrawalTj();
    tUserWithdrawalTj.setUserId(userId);
    tUserWithdrawalTj.setWithdrawalTotalMoney(withdrawalTotalMoney);
    tUserWithdrawalTj.setWithdrawalTotalMoneyUsdt(withdrawalTotalMoneyUsdt);
    tUserWithdrawalTj.setWithdrawalShareMoney(withdrawalShareMoney);
    tUserWithdrawalTj.setWithdrawalShareMoneyUsdt(withdrawalShareMoneyUsdt);
    tUserWithdrawalTj.setCreateTime(LocalDateTime.now());
    this.save(tUserWithdrawalTj);
  }

  @Override
  public void modifyTUserWithdrawalTj(String userId, String withdrawalTotalMoney,String withdrawalTotalMoneyUsdt,String withdrawalShareMoney,String withdrawalShareMoneyUsdt) {
    TUserWithdrawalTj tUserWithdrawalTj = new TUserWithdrawalTj();
    tUserWithdrawalTj.setUserId(userId);
    tUserWithdrawalTj.setWithdrawalTotalMoney(withdrawalTotalMoney);
    tUserWithdrawalTj.setWithdrawalTotalMoneyUsdt(withdrawalTotalMoneyUsdt);
    tUserWithdrawalTj.setWithdrawalShareMoney(withdrawalShareMoney);
    tUserWithdrawalTj.setWithdrawalShareMoneyUsdt(withdrawalShareMoneyUsdt);
    tUserWithdrawalTj.setCreateTime(LocalDateTime.now());
    this.updateById(tUserWithdrawalTj);
  }

}
