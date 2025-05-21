package cn.com.otc.modular.tron.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TronResponse<T> {

    private List<T> data;
    private boolean success;
    private Meta meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private long at;
        private String fingerprint;
        private Links links;
        private int pageSize;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Links {
            private String next;
        }

    }

}
