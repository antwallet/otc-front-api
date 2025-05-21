package cn.com.otc.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Data
@RefreshScope
public class NacosConstant {

    @Value("${lottery.remainingTimeBoundary}")
    private long lotteryRemainingTimeBoundary;

    @Value("${lottery.addMoreTime}")
    private long lotteryAddMoreTime;

    @Value("${lottery.addLessTime}")
    private long lotteryAddLessTime;

    @Value("${increaseRate.middle}")
    private int middleIncreaseRate;

    @Value("${increaseRate.hign}")
    private int hignIncreaseRate;

    /**
     * 印度支付手续费率
     */
    @Value("${pament.rate.indian}")
    private BigDecimal indianPamentRate;

    /**
     * 印度支付手续费,最小手续费
     */
    @Value("${pament.rate.indianMinFee}")
    private BigDecimal indianPamentMinFee;

    /**
     * 印度最低提现金额
     */
    @Value("${minimum.withdrawal.amount.indian}")
    private BigDecimal indianMinimumWithdrawalAmount;
    /**
     * 菲律宾支付手续费率
     */
    @Value("${pament.rate.philippines}")
    private BigDecimal philippinesPamentRate;

    /**
     * 菲律宾支付手续费，最小手续费
     */
    @Value("${pament.rate.philippinesMinFee}")
    private BigDecimal philippinesPamentMinFee;
    /**
     * 菲律宾最低提现金额
     */
    @Value("${minimum.withdrawal.amount.philippines}")
    private BigDecimal philippinesMinimumWithdrawalAmount;

    /**
     * #触发条件，1：新用户，2：所有用户
     */
    @Value("${pinduoduo.userType}")
    private String pinduoduoUserType;

    @Value("${pinduoduo.userScore}")
    private BigDecimal pinduoduoUserScore;

    /**
     * 日抽奖发放比率
     */
    @Value("${dailyLotteryRewardRate}")
    private BigDecimal dailyLotteryRewardRate;
    /**
     * 奖励发放后，公告发送到频道
     */
    @Value("${dailyLotteryRewardChannel}")
    private String dailyLotteryRewardChannel;
    /**
     * 查询活跃用户的开始时间
     */
    @Value("${dailyLotteryQueryUserStartTime}")
    private int dailyLotteryQueryUserStartTime;

    /**
     * 测试或者线上环境
     */
    @Value("${envType}")
    private String envType;

    @Value("${redpacketPreffix}")
    private String redpacketPreffix;
    @Value("${ignoreUrls}")
    private String ignoreUrls;
    /**
     * 低分用户固定奖励金额
     */
    @Value("${lowScoreUserAward}")
    private BigDecimal lowScoreUserAward;
    /**
     * 高分用户最低奖励金额
     */
    @Value("${hignScoreUserAward}")
    private BigDecimal hignScoreUserAward;

    @Value("${openAdChannel}")
    private String openAdChannel;
    @Value("${openAdGroup}")
    private String openAdGroup;
}
