package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.modular.sys.bean.pojo.UserIpLimit;
import cn.com.otc.modular.sys.dao.UserIpLimitDao;
import cn.com.otc.modular.sys.service.UserIpLimitService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserIpLimitServiceImpl extends ServiceImpl<UserIpLimitDao, UserIpLimit> implements UserIpLimitService {

    @Override
    public void saveUserIpLimit(UserIpLimit userIpLimit) {
        this.save(userIpLimit);
    }
}
