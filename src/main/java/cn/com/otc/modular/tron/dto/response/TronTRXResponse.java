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
public class TronTRXResponse<T> {

    private Integer total;
    private List<T> data;
    private Map<String,Object> contractMap;
    private Map<String,Object> contractInfo;
    private Integer rangeTotal;
    private Map<String,Object> normalAddressInfo;

}
