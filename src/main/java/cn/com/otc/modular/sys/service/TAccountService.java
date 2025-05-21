package cn.com.otc.modular.sys.service;

import cn.com.otc.modular.sys.bean.pojo.TAccount;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * <p>
 * 账户表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
public interface TAccountService extends IService<TAccount> {

        TAccount getAccountByUserIdAndAccountType(String userId,Integer accountType);

        void saveUserAccount(String accountId, Integer accountType, String userId, String amount);

        void deductAccountMoney(Long id,BigDecimal amount, BigDecimal deduct_amount);

        void addAccountMoney(Long id, BigDecimal amount, BigDecimal add_amount);

        TAccount selectTAccountByTgId(String tgId, String accountType);
}
