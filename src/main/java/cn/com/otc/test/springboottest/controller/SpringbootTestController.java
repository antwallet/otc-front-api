package cn.com.otc.test.springboottest.controller;

import cn.com.otc.common.utils.PageUtils;
import cn.com.otc.common.utils.R;
import cn.com.otc.test.springboottest.service.SpringbootTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/5/25
 */
@Slf4j
@RestController
@RequestMapping("/springboot")
public class SpringbootTestController {

    @Autowired
    private SpringbootTestService springbootTestService;

    @RequestMapping("/test")
    public String  test(){
       springbootTestService.test();
       log.info("返回结果");
       return "success";
    }

    @RequestMapping("/listPage")
    public R listPage(@RequestParam Map<String, Object> params){
        PageUtils page = springbootTestService.queryPage(params);
        return R.ok().put("page",page);
    }
}
