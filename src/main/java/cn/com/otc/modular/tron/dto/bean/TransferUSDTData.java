package cn.com.otc.modular.tron.dto.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferUSDTData {
    private String transaction_id;
    private Integer status;
    private Long block_ts;
    private String from_address;
    private FromAddressTag from_address_tag;
    private String to_address;
    private ToAddressTag to_address_tag;
    private Long block;
    private String contract_address;
    private TriggerInfo trigger_info;
    private String quant;
    private Integer approval_amount;
    private String event_type;
    private Boolean confirmed;
    private String contractRet;
    private String finalResult;
    private TokenInfo tokenInfo;
    private Boolean revert;
    private String contract_type;
    private Boolean fromAddressIsContract;
    private Boolean toAddressIsContract;
    private Boolean riskTransaction;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FromAddressTag {
        private String from_address_tag;
        private String from_address_tag_logo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToAddressTag {
        private String to_address_tag_logo;
        private String to_address_tag;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriggerInfo {
        private String method;
        private String data;
        private Parameter parameter;
        private String methodName;
        private String contract_address;
        private Integer call_value;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Parameter {
            private String _value;
            private String _to;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenInfo {
      private String tokenId;
      private String tokenName;
      private Integer tokenDecimal;
      private Integer tokenCanShow;
      private String tokenType;
      private String tokenLogo;
      private String tokenLevel;
      private String issuerAddr;
      private Boolean vip;
    }
}
