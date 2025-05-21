package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.modular.sys.bean.pojo.TInvitedUser;
import cn.com.otc.modular.sys.dao.TInvitedUserDao;
import cn.com.otc.modular.sys.service.TInvitedUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description:用户邀请关系表管理实现类
 * @author: zhangliyan
 * @time: 2024/4/12
 */
@Service
public class TInvitedUserServiceImpl extends ServiceImpl<TInvitedUserDao, TInvitedUser> implements
    TInvitedUserService {

  @Override
  public List<TInvitedUser> getTInvitedUserList(String userId) {
    LambdaQueryWrapper<TInvitedUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.eq(TInvitedUser::getUserId,userId);
    return this.list(lambdaQueryWrapper);
  }

  @Override
  public TInvitedUser getTInvitedUser(String invitedUserId) {
    LambdaQueryWrapper<TInvitedUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.eq(TInvitedUser::getInvitedUserId,invitedUserId);
    return this.getOne(lambdaQueryWrapper);
  }

  @Override
  public void saveTInvitedUser(String userId, String invitedUserId, String activityId, String redpacketId) {
    TInvitedUser invitedUser = new TInvitedUser();
    invitedUser.setUserId(userId);
    invitedUser.setInvitedUserId(invitedUserId);
    invitedUser.setActivityId(activityId);
    invitedUser.setRedpacketId(redpacketId);
    invitedUser.setCreateTime(LocalDateTime.now());
    this.save(invitedUser);
  }

  /**
   * 根据id更新have_reward字段
   * @param id 要更新的记录的id
   * @param haveReward 新的have_reward值
   * @return 是否更新成功
   */
  @Override
  public void updateHaveRewardById(Long id, String haveReward) {
    LambdaUpdateWrapper<TInvitedUser> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(TInvitedUser::getId, id)
            .set(TInvitedUser::getHavaReward, haveReward);

    this.update(updateWrapper);
  }
}
