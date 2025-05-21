package cn.com.otc.modular.sys.service;

import cn.com.otc.modular.sys.bean.pojo.TWalletPool;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 * 钱包地址池表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-04-01
 */
public interface TWalletPoolService extends IService<TWalletPool> {
        List<TWalletPool> getTWalletPoolList();

        TWalletPool getTWalletPoolByAddress(String address);

}
