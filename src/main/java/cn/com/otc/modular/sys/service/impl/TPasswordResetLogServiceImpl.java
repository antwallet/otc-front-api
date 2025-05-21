package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TPasswordResetLog;
import cn.com.otc.modular.sys.dao.TPasswordResetLogDao;
import cn.com.otc.modular.sys.service.TPasswordResetLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 重置密码记录表 服务实现类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
@Service
@Slf4j
public class TPasswordResetLogServiceImpl extends ServiceImpl<TPasswordResetLogDao, TPasswordResetLog> implements TPasswordResetLogService {

    @Override
    public void registerTPasswordResetLog(String tgId, String pwd) {
        TPasswordResetLog tPasswordResetLog = new TPasswordResetLog();
        tPasswordResetLog.setTgId(tgId);
        tPasswordResetLog.setPassWord(pwd);
        tPasswordResetLog.setCreateTime(LocalDateTime.now());
        this.save(tPasswordResetLog);
    }

    @Override
    public TPasswordResetLog checkPasswordResetLog(UserInfoResult userInfoResult, String lang) {
        LambdaQueryWrapper<TPasswordResetLog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TPasswordResetLog::getTgId,userInfoResult.getUserTGID());
        return this.getOne(lambdaQueryWrapper);
    }

}
