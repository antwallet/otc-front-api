package cn.com.otc.modular.sys.bean.vo;

import lombok.Data;

/**
 * @description:收款前端bean
 * @author: xiaoyao
 * @time: 2024/11/23
 */
@Data
public class TronTransVo {

  /**
   * 转发链接
   */
  private String forwardLink;
  /**
   * 收款链接
   */
  private String link;

}
