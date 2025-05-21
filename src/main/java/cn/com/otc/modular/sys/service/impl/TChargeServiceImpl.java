package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.modular.sys.dao.TChargeDao;
import cn.com.otc.modular.sys.bean.pojo.TCharge;
import cn.com.otc.modular.sys.service.TChargeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @description:充值实现类
 * @author: zhangliyan
 * @time: 2024/4/1
 */
@Service
public class TChargeServiceImpl extends ServiceImpl<TChargeDao, TCharge> implements
    TChargeService {

  @Override
  public List<TCharge> getCurrChargeDataList(Integer status) {
    LambdaQueryWrapper<TCharge> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.eq(TCharge::getStatus,status);
    return this.list(lambdaQueryWrapper);
  }

  @Override
  public TCharge queryCharge(String orderId,String userId, String base58CheckAddress,Integer chargeType, String chargeMoney, Integer status) {
    LambdaQueryWrapper<TCharge> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    if(StringUtils.isNotBlank(orderId)) {
      lambdaQueryWrapper.eq(TCharge::getOrderId, orderId);
    }
    if(StringUtils.isNotBlank(userId)) {
      lambdaQueryWrapper.eq(TCharge::getUserId, userId);
    }
    if(StringUtils.isNotBlank(base58CheckAddress)) {
      lambdaQueryWrapper.eq(TCharge::getBase58CheckAddress, base58CheckAddress);
    }
    if(chargeType != null){
      lambdaQueryWrapper.eq(TCharge::getChargeType, chargeMoney);
    }
    if(StringUtils.isNotBlank(chargeMoney)) {
      lambdaQueryWrapper.eq(TCharge::getMoney, chargeType);
    }
    if(status != null) {
      lambdaQueryWrapper.eq(TCharge::getStatus, status);
    }
    TCharge tCharge = this.getOne(lambdaQueryWrapper);
    return tCharge;
  }

  @Override
  public void createCharge(String orderId,String userId,String base58CheckAddress,Integer chargeType,String chargeMoney,String qrcodeImage) {
    LocalDateTime now = LocalDateTime.now();
    TCharge tCharge = new TCharge();
    tCharge.setOrderId(orderId);
    tCharge.setUserId(userId);
    tCharge.setBase58CheckAddress(base58CheckAddress);
    tCharge.setChargeType(chargeType);
    tCharge.setMoney(chargeMoney);
    tCharge.setQrcodeImage(qrcodeImage);
    tCharge.setCreateTime(now);
    // 往后推 30 分钟
    LocalDateTime later = now.plusMinutes(30);
    tCharge.setExpireTime(later);
    this.save(tCharge);
  }

  @Override
  public void modifyChargeStatus(Long id, Integer status,String money,Integer chargeType,
      LocalDateTime chargeTime,String chargeTxid) {
    TCharge tCharge = new TCharge();
    tCharge.setId(id);
    tCharge.setStatus(status);
    tCharge.setMoney(money);
    tCharge.setChargeType(chargeType);
    tCharge.setChargeTime(chargeTime);
    tCharge.setChargeTxid(chargeTxid);
    tCharge.setUpdateTime(LocalDateTime.now());
    this.updateById(tCharge);
  }

}
