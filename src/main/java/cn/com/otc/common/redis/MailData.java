package cn.com.otc.common.redis;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhaojx
 * @date 2020/4/21 20:04.
 */
@Data
@EqualsAndHashCode
public class MailData {
    private int playerId;
    private String title;
    private String content;
    private String reward;
    private int sendType;
    private int dropDay;
}
