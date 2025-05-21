package cn.com.otc.modular.sys.service;

import cn.com.otc.modular.auth.entity.vo.LoginUserVO;
import cn.com.otc.modular.sys.bean.pojo.TLoginRecord;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 登录记录表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
public interface TLoginRecordService extends IService<TLoginRecord> {

    void saveLoginRecord(String userId, String loginChannel);

    /**
     *
     * 功能描述: 修改最后一次登录时间
     *
     * @auther: 2024
     * @date: 2024/9/28 10:55
     */
    void updateLoginRecord(LoginUserVO loginUserVO, String ipAddress, String lang, TUser user);

}
