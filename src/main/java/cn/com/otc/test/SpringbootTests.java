package cn.com.otc.test;

import cn.com.otc.test.springboottest.service.SpringbootTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/5/17
 */
@SpringBootTest
public class SpringbootTests {

    @Autowired
    private SpringbootTestService springbootTestService;

    @Test
    public void test(){
        springbootTestService.test();
    }
}
