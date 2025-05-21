package cn.com.otc.common.enums;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;

@Getter
public enum PinduoduoStepEnum {
    STEP_1(1, BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0), "宝箱+现金卡"),
    STEP_2(2, BigDecimal.valueOf(1.68), BigDecimal.valueOf(1.68), "摇一摇任务"),
    STEP_3(3, BigDecimal.valueOf(0.5), BigDecimal.valueOf(2.18), "优质用户-官方赠送"),
    STEP_4(4, BigDecimal.valueOf(0.3), BigDecimal.valueOf(2.48), "优质用户-官方赠送"),
    STEP_5(5, BigDecimal.valueOf(0.2), BigDecimal.valueOf(2.68), "确认提现方式-官方赠送"),
    STEP_6(6, BigDecimal.valueOf(0.2), BigDecimal.valueOf(2.88), "加入频道-官方赠送", true),
    STEP_7(7, BigDecimal.valueOf(0.1), BigDecimal.valueOf(2.89), "关注bot-官方赠送", true),  // 需要设置未完成
    STEP_8(8, BigDecimal.valueOf(0.1), BigDecimal.valueOf(2.99), "小B端任务", true),
    STEP_9(9, BigDecimal.valueOf(0.0), BigDecimal.valueOf(2.99), "提现邀请",true);;

    private final int step;
    /**
     * 到目前步数的总奖励金额
     */
    private final BigDecimal reward;
    /**
     * 当前步数的奖励金额
     */
    private final BigDecimal currentStepReward;
    private final String description;
    private final boolean needsCompletion;

    PinduoduoStepEnum(int step, BigDecimal reward, BigDecimal currentStepReward, String description) {
        this(step, reward, currentStepReward, description, false);
    }

    PinduoduoStepEnum(int step, BigDecimal reward, BigDecimal currentStepReward, String description, boolean needsCompletion) {
        this.step = step;
        this.reward = reward;
        this.currentStepReward = currentStepReward;
        this.description = description;
        this.needsCompletion = needsCompletion;
    }

    public static PinduoduoStepEnum of(int step) {
        return Arrays.stream(values())
                .filter(ts -> ts.getStep() == step)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid step: " + step));
    }
}
