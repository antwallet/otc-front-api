package cn.com.otc.modular.sys.dao;

import cn.com.otc.modular.sys.bean.pojo.TUserWithdrawal;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户提现表 Mapper 接口
 * </p>
 *
 * @author zhangliyan
 * @since 2024-04-12
 */
public interface TUserWithdrawalDao extends BaseMapper<TUserWithdrawal> {
        void updateUserWithdrawalStatus(@Param("status") String status, @Param("walletId")String walletId);
        TUserWithdrawal selectByWalletId(String walletId);
}
