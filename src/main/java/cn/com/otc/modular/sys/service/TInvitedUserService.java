package cn.com.otc.modular.sys.service;

import cn.com.otc.modular.sys.bean.pojo.TInvitedUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户邀请关系表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-04-12
 */
public interface TInvitedUserService extends IService<TInvitedUser> {

        List<TInvitedUser> getTInvitedUserList(String userId);

        TInvitedUser getTInvitedUser(String invitedUserId);

        void saveTInvitedUser(String userId,String invitedUserId, String activityId, String redpacketId);

        void updateHaveRewardById(Long id, String haveReward);
}
