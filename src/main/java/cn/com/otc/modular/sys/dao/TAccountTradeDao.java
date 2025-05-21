package cn.com.otc.modular.sys.dao;

import cn.com.otc.modular.sys.bean.pojo.TAccountTrade;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 账户交易表 Mapper 接口
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
public interface TAccountTradeDao extends BaseMapper<TAccountTrade> {

        List<TAccountTrade> queryByPage(@Param("start") int start, @Param("pageSize") int pageSize, @Param("accountId") String accountId, @Param("accountType") Integer accountType, @Param("tradeType") Integer tradeType);

        Integer queryByPageCount(@Param("accountId") String accountId,@Param("accountType") Integer accountType);





}
