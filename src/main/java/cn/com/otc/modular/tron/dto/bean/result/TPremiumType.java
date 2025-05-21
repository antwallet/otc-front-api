package cn.com.otc.modular.tron.dto.bean.result;

import lombok.Data;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/7
 */
@Data
public class TPremiumType {

    //金额
    private String price;
    //时长
    private String duration;

    private String exchangePrice;
}
