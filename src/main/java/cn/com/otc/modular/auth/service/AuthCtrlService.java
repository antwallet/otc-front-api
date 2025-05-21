package cn.com.otc.modular.auth.service;

import cn.com.otc.modular.auth.entity.vo.LoginUserVO;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;

/**
 * @description:红包系统登录层接口
 * @author: zhangliyan
 * @time: 2024/2/1
 */
public interface AuthCtrlService {

     UserInfoResult reqUserInfoByToken(String token, Boolean isGetAvatar, String remortIP,
                                       Boolean isPremium, String deviceModel, String registrationDate, String lang);

     //UserInfoResult invitedUser(String token,String redpacketId, String lang);

     void resetChargePsw(String userTGId, String chargePassword, String lang);

     void registerUser(String userTGId, String logiName, String chargePassword, String lang);

     UserInfoResult getUserInfoAvatarToken(String requestToken, Boolean isGetAvatar, String lang);

     void generateUserInfo(LoginUserVO loginUserVO, String remoteIP, String lang);

     void handleUpdateUserInfo(LoginUserVO loginUserVO, String remortIP, String lang);
}
