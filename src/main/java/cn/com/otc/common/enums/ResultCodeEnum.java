package cn.com.otc.common.enums;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2022/5/12
 */
public enum ResultCodeEnum {
    /**
     * 系统全局错误码
     * 0、-1、404、500、403
     */
    SUCCESS(0, "success"),
    FAIL(-1, "fail"),
    SYSTEM_ERROR_404(404, "Interface Not Found"),
    SYSTEM_ERROR_500(500, "Interface %s internal error, please contact the administrator"),
    SYSTEM_LANGUAGE_CODE_ERROR(600, "The language encoding is not supported by the system"),
    ILLEGAL_PARAMETER(1000, "Illegal parameter"),
    PARAM_NOT_EXIST(1001, "%s does not exist"),

    EXPORT_ERROR_2001(2001, "noauth export"),
    EXPORT_ERROR_2002(2002, "create file error"),

    GEN_ERROR_3001(3001, "代码生成失败"),

    //发红包
    ILLEGAL_ACCOUNT_TYPE(4001,"无效的账户类型"),
    USER_IS_NOT_EXIST(4002,"用户不存在"),
    REDPACKET_MONEY_LIMIT_ERROR(4003,"红包金额格式错误"),
    ACCOUNT_IS_NOT_EXIST(4004,"账户不存在"),
    ILLEGAL_REDPACKET_TYPE(4005,"无效的群发红包类型"),
    REDPACKET_NUM_LIMIT(4006,"红包数量超过群聊人数"),
    SINGLE_REDPACKET_IS_NOT_EXIST(4007,"个人红包不存在"),
    TTRONTRANS_IS_NOT_EXIST(4007,"收款记录不存在"),
    REDPACKET_IS_RECEIVE(4008,"红包已领取"),
    REDPACKET_IS_EXPIRE(4009,"红包已过期"),
    REDPACKET_RECEIVE_ERROR(4010,"红包领取失败"),
    REDPACKET_RECEIVE_FINISHED(4011,"红包已领取完"),
    REDPACKET_IS_NOT_RECEIVE(4012,"红包未领取"),
    GROUP_REDPACKET_IS_NOT_EXIST(4013,"群发红包不存在"),
    REDPACKETID_ERROR(4014,"红包ID错误"),
    TREFUNDAMOUNTRECORD_IS_NOT_EXIST(4015,"退款记录不存在"),
    ILLEGAL_TRANS_TYPE(4016,"无效的收款类型"),
    TTRONTRANS_IS_EXPIRE(4017,"收款已过期"),



    CHARGE_ADDRESS_IS_NOT_EXIST(5001,"充值钱包地址不存在"),
    CHARGE_ADDRESS_IS_USEED(5002,"充值钱包地址被占用"),
    CHARGE_ORDER_IS_NOT_EXIST(5003,"充值订单不存在"),
    CHARGE_ORDER_STATUS_IS_ERROR(5004,"充值订单状态失败"),
    CHARGE_CANCEL_LIMIT(5005,"取消充值订单超过了次数"),
    CHARGE_MONEY_LIMIT(5006,"充值金额小于1"),
    TRUNSFER_TRX_NOT_ENOUGH(5007,"TRX转账宽带不足"),
    TRUNSFER_USDT_NOT_ENOUGH(5008,"USDT转账能量或者宽带不足"),
    TRUNSFER_TRX_NOT_LIMIT(5009,"TRX转账没有达到阈值"),
    TRUNSFER_USDT_NOT_LIMIT(5010,"USDT转账没有达到阈值"),
    TRUNSFER_NOT_REFRESH(5011,"不允许刷新状态"),

    USER_PSW_EXIST(6001,"新密码和老密码一致"),
    USER_LOGINNAME_EXIST(6002,"注册账号已存在"),
    USER_PSW_ERROR(6003,"支付密码错误"),
    USER_BINDED_EEROR(6004,"用户绑定失败"),
    USER_REMOVE_BINDED_EEROR(6004,"删除用户绑定失败"),

    TG_HTTP_ERROR(7001,"通知TG失败"),

    TRONTRANS_IS_NOT_EXIST(8004,"收款不存在"),
    TRONTRANS_SENDUSER_RECEIVE_SAME(8005,"收款用户和付款用户一致"),

    WITHDRAWAL_NOT_ENOUGH(9001,"提现金额不足"),
    WITHDRAWAL_AUTO_ERROR(9002,"自动提现失败"),
    WITHDRAWAL_IS_NOT_EXSIT(9003,"提现数据不存在"),

    PREMIUM_BUY_PRICE_ERROR(10001,"购买会员价格错误"),
    //PREMIUM_BUY_NOT_ENOUGH(10002,"购买会员金额不足"),

    MONEY_NOT_ENOUGH(10003,"余额不足"),
    ENERGY_BUY_ERROR(10004,"购买能量失败"),
    ENERGY_BUY_NO_FAST_CHARGE(10005,"购买能量不支持速充"),

    //活动
    ACTIVITY_IS_NOT_EXIST(701,"活动不存在"),
    NO_CORRESPONDING_PASSCODE(702,"无对应的口令码，请联系管理员哦!!!"),

    ILLEGAL_ACCESS_MINI_PROGRAMS_ERROR(601,"违法进入小程序"),;
    public Integer code;

    public String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
