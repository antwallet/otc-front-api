package cn.com.otc.modular.sys.service;

import cn.com.otc.modular.sys.bean.pojo.TUserWithdrawalTj;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户提现手续费统计 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-04-12
 */
public interface TUserWithdrawalTjService extends IService<TUserWithdrawalTj> {

        TUserWithdrawalTj getTUserWithdrawalTj(String userId);

        void saveTUserWithdrawalTj(String userId,String withdrawalTotalMoney,String withdrawalTotalMoneyUsdt,String withdrawalShareMoney,String withdrawalShareMoneyUsdt);

        void modifyTUserWithdrawalTj(String userId,String withdrawalTotalMoney,String withdrawalTotalMoneyUsdt,String withdrawalShareMoney,String withdrawalShareMoneyUsdt);

}
