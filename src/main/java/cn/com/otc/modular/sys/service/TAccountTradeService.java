package cn.com.otc.modular.sys.service;

import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.PageUtils;
import cn.com.otc.modular.dict.entity.pojo.SysDictData;
import cn.com.otc.modular.sys.bean.result.QueryAccountTradeByPageRequest;
import cn.com.otc.modular.sys.bean.pojo.TAccountTrade;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 账户交易表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
public interface TAccountTradeService extends IService<TAccountTrade> {
        PageUtils queryPage(Map<String, Object> params);

        TAccountTrade selectAccountTradeByUserId(String userId,Integer tradeType);

        BigDecimal getAccountTradeByUserId(String userId, String tradeNo);

        void saveAccountTrade(String accountId,String userId,String tradeNo,
            String money,Integer tradeType,String leftAmount,Integer accountType,Integer receiveNum);


        void saveAccountTradeCharge(String accountId,String userId,String tradeNo,
            String money,Integer tradeType,String leftAmount,Integer accountType);

        ResponseEntity<?> queryByPage(QueryAccountTradeByPageRequest request, String lang);

        /**
         * 功能描述: 获取所有领取分享红包的记录
         *
         * @return
         * @auther: 2024
         * @date: 2024/10/14 14:17
         */
        List<TAccountTrade> selectAccountTradeList();


        List<SysDictData> displayTransactionTypes(String lang);

}
