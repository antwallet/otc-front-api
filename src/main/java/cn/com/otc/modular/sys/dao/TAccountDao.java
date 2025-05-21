package cn.com.otc.modular.sys.dao;

import cn.com.otc.modular.sys.bean.pojo.TAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 账户表 Mapper 接口
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
public interface TAccountDao extends BaseMapper<TAccount> {

        TAccount selectTAccountByTgId(@Param("userId") String userId, @Param("accountType")String accountType);
}
