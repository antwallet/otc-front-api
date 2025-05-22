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

    /*
    * 重置密码
    * */
    void resetPassword(UserInfoResult userInfoResult, String lang);


    /*
     * 更新语言
     * */
    void updateUserLanguage(Long id, String lang);

}
