package cn.com.otc.modular.tron.dto.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferTRXData {
    private String id;
    private Long block;
    private String transactionHash;
    private Long timestamp;
    private String transferFromAddress;
    private String transferToAddress;
    private Long amount;
    private String tokenName;
    private Boolean confirmed;
    private String data;
    private String contractRet;
    private Boolean revert;
    private Boolean cheatStatus;
    private TokenInfo tokenInfo;
    private Boolean riskTransaction;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenInfo {
        private String tokenId;
        private String tokenAbbr;
        private String tokenName;
        private Integer tokenDecimal;
        private Integer tokenCanShow;
        private String tokenType;
        private String tokenLogo;
        private String tokenLevel;
        private Boolean vip;
    }
}
