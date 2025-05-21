package cn.com.otc.test.springboottest.service.impl;

import cn.com.otc.common.utils.PageUtils;
import cn.com.otc.common.utils.Query;
import cn.com.otc.modular.dict.dao.SysDictDataDao;
import cn.com.otc.modular.dict.entity.pojo.SysDictData;
import cn.com.otc.test.springboottest.service.SpringbootTestService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/5/25
 */
@Slf4j
@Service
@DS("slave")
public class SpringbootTestServiceImpl extends ServiceImpl<SysDictDataDao, SysDictData> implements SpringbootTestService {

    @Async
    @Override
    public void test() {
        log.info("开始业务逻辑");
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("完成业务逻辑");
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SysDictData> page = this.page(
                new Query<SysDictData>().getPage(params),
                new QueryWrapper<SysDictData>());
        return new PageUtils(page);
    }

}
