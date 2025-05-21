package cn.com.otc.modular.tron.dto.bean;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TransferChargeData {
    private String address; //钱包地址
    private String money; //充值金额
    private Integer chargeType; //充值类型 TRX\USDT
    private LocalDateTime chargeTime; //充值时间
    private String chargeTxid; //充值txid
}
