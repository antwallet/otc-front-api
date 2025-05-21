package cn.com.otc.modular.tron.dto.bean;

import cn.hutool.core.util.HexUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tron.trident.utils.Base58Check;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferData {

    private List<Ret> ret;
    private List<String> signature;
    private String txID;
    private int netUsage;
    private String rawDataHex;
    private int netFee;
    private int energyUsage;
    private long blockNumber;
    private long blockTimestamp;
    private long energyFee;
    private long energyUsageTotal;
    private RawData rawData;
    private List<String> internalTransactions;

    public long getAmount() {
        if (rawData != null && rawData.getContract() != null && !rawData.getContract().isEmpty()
                && rawData.getContract().get(0).getParameter() != null
                && rawData.getContract().get(0).getParameter().getValue() != null
        ) {
            return rawData.getContract().get(0).getParameter().getValue().getAmount() / 1000000;
        }
        return 0;
    }

    public String getFrom() {
        if (rawData != null && rawData.getContract() != null && !rawData.getContract().isEmpty()
                && rawData.getContract().get(0).getParameter() != null
                && rawData.getContract().get(0).getParameter().getValue() != null
        ) {
            String result = rawData.getContract().get(0).getParameter().getValue().getOwnerAddress();
            if (result != null && !result.isEmpty()) {
                return Base58Check.bytesToBase58(HexUtil.decodeHex(result));
            }
            return result;
        }
        return null;
    }

    public String getTo() {
        if (rawData != null && rawData.getContract() != null && !rawData.getContract().isEmpty()
                && rawData.getContract().get(0).getParameter() != null
                && rawData.getContract().get(0).getParameter().getValue() != null
        ) {
            String result = rawData.getContract().get(0).getParameter().getValue().getToAddress();
            if (result != null && !result.isEmpty()) {
                return Base58Check.bytesToBase58(HexUtil.decodeHex(result));
            }
            return result;
        }
        return null;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ret {
        private String contractRet;
        private long fee;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RawData {
        private List<Contract> contract;
        private String ref_block_bytes;
        private String ref_block_hash;
        private long expiration;
        private long timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contract {
        private String type;
        private Parameter parameter;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Parameter {
            private String typeUrl;
            private Value value;

            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Value {
                private long amount;
                private String ownerAddress;
                private String toAddress;
            }
        }
    }
}
