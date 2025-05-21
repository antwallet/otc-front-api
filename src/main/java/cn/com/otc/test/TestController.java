package cn.com.otc.test;

import cn.com.otc.common.exception.RRException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/5/16
 */
@RestController
public class TestController {

    @RequestMapping("/test")
    public String test(){
        test1();
        return "hello springboot";
    }

    private void test1(){
        /*String a = "123a";
        Integer b = Integer.valueOf(a);*/
        throw new RRException("出错1111",201);
    }
}
