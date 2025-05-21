package cn.com.otc.modular.sys.service;

import cn.com.otc.common.utils.PageUtils;
import cn.com.otc.modular.auth.entity.vo.LoginUserVO;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
public interface TUserService extends IService<TUser> {

    TUser getTUser(String TGId);

    PageUtils queryPage(Map<String, Object> params);

    TUser getTUserByUserId(String userId);

    TUser getTUserByTGId(String TGId);

    void registerTUser(String userId, String tgId, String name, String nick, String avatar, String pwd, String remortIP,
                       Boolean isPremium, String deviceModel, String registrationDate, BigDecimal userScore, String lang, String resolution, String networkStatus, String networkType);

    void updateRedPacketCover(Long userId, String redpacketImg);

    void updateUserAvatar(Long userId, String avatar);

    void updateUserIsShowPanel(Long userId, Short isShowPanel);

    void updateUserScoreAndCaptcha(Long userId, BigDecimal userScore, String captchaVerified);

    void updateUserScoreAndImgVerified(Long userId, BigDecimal userScore, String userImgVerified);

    void updateUserScore(TUser tUser);


    void updateUser(Long userId, String name, String nick);


    List<TUser> queryByUserIdList(List<String> userIdList);

    //分页查询数据
    List<TUser> getTUserByUserIds(List<String> tgIdList);

    /*
    * 重置密码
    * */
    void resetPassword(UserInfoResult userInfoResult, String lang);

    void updateUserIpAddress(Boolean isPremium, String deviceModel, String ipAddress, Long id);
    //修改设备型号、IP地址、注册时间、是否为会员
    void handleUpdateUserInfo(LoginUserVO loginUserVO, String remortIP, Long id, String lang);

    // 批量获取用户的信息
    List<TUser> getTUserByTGIds(List<String> keys);

    /*
     * 更新语言
     * */
    void updateUserLanguage(Long id, String lang);

}
