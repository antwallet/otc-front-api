package cn.com.otc.modular.sys.service;

import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.bean.pojo.TUserBuyPremium;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 用户购买会员表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-07-14
 */
public interface TUserBuyPremiumService extends IService<TUserBuyPremium> {

        void saveTUserBuyPremium(String premiumBuyId,String payUserId,String buyUserId,
            String buyUserName,Integer premiumType,String money,Integer accountType);

        List<TUserBuyPremium> list(TUser tUser);

        ResponseEntity<?> getChat(String userName, String lang);

        R handlePremiumType(String lang, HttpServletRequest httpRequest, String exchangeRateType);

}
