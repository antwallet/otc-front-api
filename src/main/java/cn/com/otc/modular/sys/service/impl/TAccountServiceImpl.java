package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.modular.sys.bean.pojo.TAccount;
import cn.com.otc.modular.sys.dao.TAccountDao;
import cn.com.otc.modular.sys.service.TAccountService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 账户表 服务实现类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
@Service
public class TAccountServiceImpl extends ServiceImpl<TAccountDao, TAccount> implements TAccountService {

  @Override
  public TAccount getAccountByUserIdAndAccountType(String userId,Integer accountType) {
    LambdaQueryWrapper<TAccount> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.eq(TAccount::getUserId,userId);
    lambdaQueryWrapper.eq(TAccount::getAccountType,accountType);
    return this.getOne(lambdaQueryWrapper);
  }

  @Override
  public void saveUserAccount(String accountId, Integer accountType, String userId, String amount) {
    TAccount tAccount = new TAccount();
    tAccount.setAccountId(accountId);
    tAccount.setAccountType(accountType);
    tAccount.setUserId(userId);
    tAccount.setAmount(amount);
    tAccount.setCreateTime(LocalDateTime.now());
    this.save(tAccount);
  }

  /**
   * 扣除金额
   * @param id
   * @param amount
   * @param deduct_amount
   */
  @Override
  public void deductAccountMoney(Long id, BigDecimal amount, BigDecimal deduct_amount) {
    BigDecimal amount_new = amount.subtract(deduct_amount);
    TAccount tAccount = new TAccount();
    tAccount.setId(id);
    tAccount.setAmount(String.valueOf(amount_new));
    tAccount.setUpdateTime(LocalDateTime.now());
    this.updateById(tAccount);
  }

  /**
   * 增加金额
   * @param id
   * @param amount
   * @param add_amount
   */
  @Override
  public void addAccountMoney(Long id, BigDecimal amount, BigDecimal add_amount) {
    BigDecimal amount_new = amount.add(add_amount);
    TAccount tAccount = new TAccount();
    tAccount.setId(id);
    tAccount.setAmount(amount_new.toString());
    tAccount.setUpdateTime(LocalDateTime.now());
    this.updateById(tAccount);
  }

  @Override
  public TAccount selectTAccountByTgId(String tgId, String accountType) {
    return this.baseMapper.selectTAccountByTgId(tgId,accountType);
  }

}
