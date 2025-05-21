package cn.com.otc.modular.tron.dto.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawUSDTData {
    private String energy; //能量
    private String txid; //充值txid
}
