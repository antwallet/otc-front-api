package cn.com.otc.modular.sys.service;

import cn.com.otc.common.utils.PageUtils;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TPasswordResetLog;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * <p>
 * 重置密码记录表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
public interface TPasswordResetLogService extends IService<TPasswordResetLog> {

    void registerTPasswordResetLog(String tgId, String pwd);

    TPasswordResetLog checkPasswordResetLog(UserInfoResult userInfoResult, String lang);

}
