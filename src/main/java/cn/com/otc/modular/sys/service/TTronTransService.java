package cn.com.otc.modular.sys.service;

import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.result.TronTransRequest;
import cn.com.otc.modular.sys.bean.pojo.TTronTrans;
import cn.com.otc.modular.sys.bean.vo.TronTransVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 收款表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-03-26
 */
public interface TTronTransService extends IService<TTronTrans> {

        TTronTrans getTTronTrans(String tronTransId);

        void saveTTronTrans(String tronTransId, String sendUserId, Integer accountType, String money, Integer transNum,
                            Integer transType, String groupsConditions, String channelConditions, String comment, Integer paymentExpiryTime, Integer subscriptionExpiryTime,
                            String subscriptionDesc,String customerServiceLink,String sharingRatio);


        void modifyTTronTransStatus(Long id,Integer status);

        void addTronTransPaymentCount(TTronTrans tTronTrans, int i);

        /**
         * 处理过期未申述付款
         */
        //void handlePaymentRecordsExpire();


        List<TTronTrans> getTTronTransByGroupsName(String chatUserName);

        List<TTronTrans> getTTronTransByChannelName(String chatUserName);

        /*
        * 查看用户发起收款的记录
        * */
        List<TTronTrans> tTronTransList(UserInfoResult userInfoResult, String lang);

        void handleStopPayment(String id, String lang);

}
