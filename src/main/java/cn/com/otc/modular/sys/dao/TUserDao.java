package cn.com.otc.modular.sys.dao;

import cn.com.otc.modular.sys.bean.pojo.TUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
public interface TUserDao extends BaseMapper<TUser> {

    List<TUser> queryByUserIdList(@Param("list")List<String> userIdList);

    Integer updateUserPwdByTgId(@Param("tgId") String tgId, @Param("password") String password);

    List<TUser>  getTUserByTGIds(@Param("keys") List<String> keys);
    List<TUser>  getTUserByUserIds(@Param("list")List<String> userIdList);
}
