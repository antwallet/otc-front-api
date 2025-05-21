package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.modular.auth.entity.vo.LoginUserVO;
import cn.com.otc.modular.sys.bean.pojo.TLoginRecord;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.dao.TLoginRecordDao;
import cn.com.otc.modular.sys.service.TLoginRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * <p>
 * 登录记录表 服务实现类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
@Service

public class TLoginRecordServiceImpl extends ServiceImpl<TLoginRecordDao, TLoginRecord> implements TLoginRecordService {

  @Autowired
  private TUserServiceImpl userService;
  private static final String LOGIN_CHANNEL_TG = "TG";
  @Override
  public void saveLoginRecord(String userId, String loginChannel) {
    TLoginRecord tLoginRecord = new TLoginRecord();
    tLoginRecord.setUserId(userId);
    tLoginRecord.setLoginChannel(loginChannel);
    tLoginRecord.setLoginTime(LocalDateTime.now());
    tLoginRecord.setCreateTime(LocalDateTime.now());
    tLoginRecord.setLastLoginTime(LocalDateTime.now());
    this.save(tLoginRecord);
  }

  @Override
  public void updateLoginRecord(LoginUserVO loginUserVO, String ipAddress, String lang, TUser user) {

    LambdaQueryWrapper<TLoginRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.eq(TLoginRecord::getUserId, user.getUserId());
    TLoginRecord tLoginRecord = this.getOne(lambdaQueryWrapper);
    if (tLoginRecord == null) {
      saveLoginRecord(user.getUserId(), LOGIN_CHANNEL_TG);
      return;
    }
    /*if (null != tLoginRecord.getLastLoginTime() && isInSevenDays(tLoginRecord.getLastLoginTime())) {
      // 修改用户的ip地址、是否为会员、以及设备信息
      userService.updateUserIpAddress(loginUserVO.getIsPremium(), loginUserVO.getDeviceModel(), ipAddress, user.getId());
    }*/
    this.baseMapper.updateLoginRecord(user.getUserId());
  }
  /**
   * 功能描述: 判断是否在七天内
   *
   * @auther: 2024
   * @date: 2024/7/23 10:59
   */
  private boolean isInSevenDays(LocalDateTime createTime) {
    LocalDateTime now = LocalDateTime.now();
    // 计算两个时间之间的差值
    Duration duration = Duration.between(createTime, now);
    // 检查是否在7天内
    return duration.toDays() >= 7;
  }

}
