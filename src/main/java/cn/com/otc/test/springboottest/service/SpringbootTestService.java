package cn.com.otc.test.springboottest.service;

import cn.com.otc.common.utils.PageUtils;

import cn.com.otc.modular.dict.entity.pojo.SysDictData;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/5/25
 */
public interface SpringbootTestService extends IService<SysDictData> {

     void test();

     PageUtils queryPage(Map<String, Object> params);
}
