package cn.com.otc.modular.sys.service;

import cn.com.otc.modular.sys.bean.pojo.TUserWithdrawal;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 * 用户提现表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-04-12
 */
public interface TUserWithdrawalService extends IService<TUserWithdrawal> {
        List<TUserWithdrawal> getCurrCheckOkTUserWithdrawalList();

        TUserWithdrawal getTUserWithdrawalByStatus(String userId,String hexAddress,
            Integer accountType,Integer status);

        void saveTUserWithdrawal(String walletId,String userId,String hexAddress,Integer blockchainType,
            Integer accountType,String money,String withdrawalMoney,String withdrawalRate,String bankName
                , String bankCardName, String bankCardAccount, String IFSC, String pinduoduo,String philippinesWithdrawalType);

        void updateTUserWithdrawal(Long id,String withdrawalTxid,Integer status,String withdrawal_usdt_blockchain_fee);


        int getTUserWithdrawalByBlockchainType(String userId,String blockchain_type);
}
