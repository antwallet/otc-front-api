package cn.com.otc.common.constants;

/**
 * @description:常量
 * @author: zhangliyan
 * @time: 2022/5/12
 */
public class CommonConstant {

  public static final String SINGLE_REDPACKET_REDIS_KEY = "antwalletbot:singleredpacket";

  public static final String GROUP_REDPACKET_REDIS_KEY = "antwalletbot:groupredpacket";

  public static final String CHARGE_REDIS_KEY = "antwalletbot:charge";

  public static final String CHARGE_CANCEL_REDIS_KEY = "antwalletbot:charge:cancel";

  public static final String LUCKY_GROUP_REDPACKET_REDIS_KEY = "antwalletbot:groupredpacket:lucky";

  public static final String TRON_TRANS_REDIS_KEY = "antwalletbot:tronTrans";

  public static final String REDPACKET_TYPE_COMMON = "0";

  public static final String REDPACKET_TYPE_LUCKY = "1";

  /**
   * 活动前缀
   */
  public static final String ACTIVITY_PREFIX = "manageServer:activityInfo:";

  /**
   * 发送的红包信息
   */
  public static final String SEND_GROUP_REDPACKET = "frontApi:sendGroupRedpacket:";
  /**
   * 红包领取
   */
  public static final String SEND_GROUP_REDPACKET_REVIEVE_NUM = "frontApi:sendGroupRedpacketRecieveNum:";

  /**
   * 用户评分
   */
  public static final String USER_SCORE_CONFIG = "manageServer:userScoreConfig";

  // 红包剩余金额
  public static final String SHARE_REDPACKET_LEAVE_AMOUNT = "frontApi:shareRedpacketLeaveAmount:";  // 红包剩余总金额

  public static final String COOPERATION_APPLICATION = "manageServer:cooperationApplication:";

  /**
   * 抽奖配置信息
   */
  public static final String LOTTERY_CONFIG = "manageServer:lotteryConfig";

  //private static final String RED_PACKET_COUNT_KEY = "frontApi:shareRedpacketTotalCount:";    // 红包个数
  //private static final String RED_PACKET_RECORD_KEY = "frontApi:shareRedpacketRecieveRecord:"; // 领取记录

    /**
     * 口令码的前缀
     */
    public static final String PASSWORD_CODE_PREFIX_REDIS_KEY = "antwalletbot:passwordCode:";

  public static final String LOTTERY_OPEN_STATUS = "manageServer:lotteryOpenStatus";
  public static final String LOTTERY_REMAINING_SECONDS = "lottery:remainingSeconds";  // 倒计时剩余秒数
}
