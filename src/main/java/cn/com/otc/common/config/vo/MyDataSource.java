package cn.com.otc.common.config.vo;

import lombok.Data;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/6/10
 */
@Data
public class MyDataSource {
    private String driverClassName;
    private String url;
    private String userName;
    private String password;
}
