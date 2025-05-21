package cn.com.otc.modular.tron.dto.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TronUSDTResponse<T> {

    private Integer total;
    private Map<String,Object> contractInfo;
    private Integer rangeTotal;
    private List<T> token_transfers;
    private Map<String,Object> normalAddressInfo;

}
