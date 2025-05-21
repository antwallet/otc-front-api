package cn.com.otc.test;

import cn.com.otc.common.utils.I18nUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Auther: 2024
 * @Date: 2024/9/5 11:39
 * @Description: 测试国家化配置
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class I18nTest {
    private final HttpServletRequest request;

    @GetMapping("/i18n")
    public String i18n() {
        String message1 = I18nUtil.getMessage("A1001", request.getHeader("lang"));
        String message2 = I18nUtil.getMessage("A1002", request.getHeader("lang"));
        String message3 = I18nUtil.getMessage("A1003", request.getHeader("lang"));
        return message1 + message2 + message3;
    }
}
