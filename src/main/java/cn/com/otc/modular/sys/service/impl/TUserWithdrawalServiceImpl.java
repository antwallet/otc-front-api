package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.modular.sys.bean.pojo.TUserWithdrawal;
import cn.com.otc.modular.sys.dao.TUserWithdrawalDao;
import cn.com.otc.modular.sys.service.TUserWithdrawalService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @description:用户提现表管理实现类
 * @author: zhangliyan
 * @time: 2024/4/12
 */
@Service
public class TUserWithdrawalServiceImpl extends ServiceImpl<TUserWithdrawalDao, TUserWithdrawal> implements
    TUserWithdrawalService {

  private static final int check_withdrawal_status_1 = 1;

  @Override
  public List<TUserWithdrawal> getCurrCheckOkTUserWithdrawalList() {
    LambdaQueryWrapper<TUserWithdrawal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.eq(TUserWithdrawal::getStatus,check_withdrawal_status_1);
    // 获取当前时间
    LocalDateTime currentDateTime = LocalDateTime.now();
    // 获取当前日期
    LocalDate currentDate = currentDateTime.toLocalDate();
    // 获取前一天的日期
    LocalDate previousDate = currentDate.minusDays(1);

    // 构造前一天凌晨的时间
    LocalDateTime previousMidnight = LocalDateTime.of(previousDate, LocalTime.MIDNIGHT);
    // 构造当前凌晨的时间
        LocalDateTime currentMidnight = LocalDateTime.of(currentDate, LocalTime.MIDNIGHT);
    // 定义日期时间格式
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    lambdaQueryWrapper.gt(TUserWithdrawal::getUpdateTime,previousMidnight.atZone(ZoneId.of("Asia/Shanghai")).format(formatter));
    lambdaQueryWrapper.lt(TUserWithdrawal::getUpdateTime,currentMidnight.atZone(ZoneId.of("Asia/Shanghai")).format(formatter));
    return this.list(lambdaQueryWrapper);
  }

  @Override
  public TUserWithdrawal getTUserWithdrawalByStatus(String userId,String hexAddress,
      Integer accountType, Integer status) {
    LambdaQueryWrapper<TUserWithdrawal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.eq(TUserWithdrawal::getUserId,userId);
    lambdaQueryWrapper.eq(TUserWithdrawal::getHexAddress,hexAddress);
    lambdaQueryWrapper.eq(TUserWithdrawal::getAccountType,accountType);
    lambdaQueryWrapper.eq(TUserWithdrawal::getStatus,status);
    return this.getOne(lambdaQueryWrapper);
  }

  @Override
  public void saveTUserWithdrawal(String walletId, String userId, String hexAddress,Integer blockchainType,
      Integer accountType, String money, String withdrawalMoney, String withdrawalRate,String bankName
          , String bankCardName, String bankCardAccount, String IFSC, String pinduoduo, String philippinesWithdrawalType) {
    TUserWithdrawal tUserWithdrawal = new TUserWithdrawal();
    tUserWithdrawal.setWalletId(walletId);
    tUserWithdrawal.setUserId(userId);
    tUserWithdrawal.setHexAddress(hexAddress);
    tUserWithdrawal.setBlockchainType(blockchainType);
    tUserWithdrawal.setAccountType(accountType);
    tUserWithdrawal.setMoney(money);
    tUserWithdrawal.setWithdrawalMoney(withdrawalMoney);
    tUserWithdrawal.setWithdrawalRate(withdrawalRate);
    tUserWithdrawal.setCreateTime(LocalDateTime.now());
    tUserWithdrawal.setPinDuoDuo(pinduoduo);
    if (StringUtils.isNotBlank(philippinesWithdrawalType)){
      tUserWithdrawal.setPhilippinesWithdrawalType(philippinesWithdrawalType);
    }
    if (StringUtils.isNotBlank(bankName)){
      tUserWithdrawal.setBankName(bankName);
    }
    if (StringUtils.isNotBlank(bankCardName)){
      tUserWithdrawal.setBankCardName(bankCardName);
    }
    if (StringUtils.isNotBlank(bankCardAccount)){
      tUserWithdrawal.setBankCardAccount(bankCardAccount);
    }
    if (StringUtils.isNotBlank(bankCardAccount)){
      tUserWithdrawal.setBankCardAccount(bankCardAccount);
    }
    if (StringUtils.isNotBlank(IFSC)){
      tUserWithdrawal.setIFSC(IFSC);
    }
    this.save(tUserWithdrawal);
  }

  @Override
  public void updateTUserWithdrawal(Long id, String withdrawalTxid, Integer status, String withdrawalUsdtBlockchainFee) {
    TUserWithdrawal tUserWithdrawal = new TUserWithdrawal();
    tUserWithdrawal.setId(id);
    tUserWithdrawal.setWithdrawalTxid(withdrawalTxid);
    tUserWithdrawal.setStatus(status);
    if (withdrawalUsdtBlockchainFee != null){
      tUserWithdrawal.setWithdrawalBlockchainFee(withdrawalUsdtBlockchainFee);
    }
    tUserWithdrawal.setUpdateTime(LocalDateTime.now());
    this.updateById(tUserWithdrawal);
  }

  @Override
  public int getTUserWithdrawalByBlockchainType(String userId, String blockchain_type) {
    LambdaQueryWrapper<TUserWithdrawal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.eq(TUserWithdrawal::getUserId,userId);
    lambdaQueryWrapper.eq(TUserWithdrawal::getBlockchainType,blockchain_type);
    return this.count(lambdaQueryWrapper);
  }

}
