package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.bean.pojo.TUserBuyEnergy;
import cn.com.otc.modular.sys.dao.TUserBuyEnergyDao;
import cn.com.otc.modular.sys.service.TUserBuyEnergyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description:用户购买能量表实现类
 * @author: zhangliyan
 * @time: 2024/7/14
 */
@Service
public class TUserBuyEnergyServiceImpl extends ServiceImpl<TUserBuyEnergyDao, TUserBuyEnergy> implements
        TUserBuyEnergyService {

    @Override
    public void saveTUserBuyEnergy(String energyBuyId, String payUserId, String money, String address, Integer energyType,
                                   Integer rentTime, Integer accountType, Integer status, String payHash) {

        TUserBuyEnergy tUserBuyEnergy = new TUserBuyEnergy();
        tUserBuyEnergy.setEnergyBuyId(energyBuyId);
        tUserBuyEnergy.setPayUserId(payUserId);
        tUserBuyEnergy.setEnergyType(energyType);
        tUserBuyEnergy.setMoney(money);
        tUserBuyEnergy.setHexAddress(address);
        tUserBuyEnergy.setAccountType(accountType);
        tUserBuyEnergy.setRentTime(rentTime);
        tUserBuyEnergy.setCreateTime(LocalDateTime.now());
        tUserBuyEnergy.setStatus(status);
        tUserBuyEnergy.setPayHash(payHash);
        this.save(tUserBuyEnergy);
    }

    @Override
    public List<TUserBuyEnergy> list(TUser tUser) {
        QueryWrapper<TUserBuyEnergy> payUserId = new QueryWrapper<TUserBuyEnergy>().eq("pay_user_id", tUser.getTgId());
        return this.list(payUserId);
    }
}
