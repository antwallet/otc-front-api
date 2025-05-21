package cn.com.otc.modular.sys.service;

import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.bean.pojo.TUserBuyEnergy;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户购买能量表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-07-14
 */
public interface TUserBuyEnergyService extends IService<TUserBuyEnergy> {

    void saveTUserBuyEnergy(String energyBuyId, String payUserId, String money,String address, Integer energyType,
                            Integer rentTime, Integer accountType, Integer status, String payHash);

    List<TUserBuyEnergy> list(TUser tUser);

}
