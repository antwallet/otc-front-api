package cn.com.otc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BotOrMiniappEnum {
    BOT("3", "机器人"),
    MINIAPP("4", "小程序"),
    TWITTER("5", "twitter");
    private final String code;
    private final String desc;

    public static boolean contains(String code) {
        for (BotOrMiniappEnum value : BotOrMiniappEnum.values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
