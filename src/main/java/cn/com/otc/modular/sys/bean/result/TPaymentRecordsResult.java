package cn.com.otc.modular.sys.bean.result;

import cn.com.otc.modular.sys.bean.pojo.TPaymentRecords;
import lombok.Data;

import java.util.List;

/**
 * @description:付款前端bean
 * @author: zhangliyan
 * @time: 2024/2/1
 */
@Data
public class TPaymentRecordsResult {

  /**
   * 总条数
   */
  private Integer count;

  private List<TPaymentRecords> list;

}
