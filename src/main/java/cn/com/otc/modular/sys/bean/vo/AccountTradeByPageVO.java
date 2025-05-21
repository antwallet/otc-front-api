package cn.com.otc.modular.sys.bean.vo;

import cn.com.otc.modular.sys.bean.result.TAccountTradeResult;
import lombok.Data;

import java.util.List;

@Data
public class AccountTradeByPageVO {
    private Integer totalCount;
    private List<TAccountTradeResult> list;
}
