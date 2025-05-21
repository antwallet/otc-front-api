package cn.com.otc.modular.sys.dao;

import cn.com.otc.modular.sys.bean.pojo.TLoginRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 登录记录表 Mapper 接口
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
public interface TLoginRecordDao extends BaseMapper<TLoginRecord> {

    /**
     * 功能描述: 判断是否是新用户
     *
     * @auther: 2024
     * @date: 2024/7/8 下午4:05
     */
    TLoginRecord queryTLoginRecordByUserId(String tgId);


    void updateLoginRecord(String userId);

}
