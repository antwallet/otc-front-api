package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.HttpStatus;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.sys.bean.result.QueryAccountTradeByPageRequest;
import cn.com.otc.modular.sys.service.TAccountTradeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/4
 */
@Slf4j
@RestController
@RequestMapping("/api/front/taccounttrade")
public class TAccountTradeController {
  @Autowired
  private TAccountTradeService tAccountTradeService;

  /**
   * 根据token获取用户账户交易信息
   * @return
   */
  @PostMapping("/list")
  public ResponseEntity<?> list(HttpServletRequest httpRequest, @Valid @RequestBody QueryAccountTradeByPageRequest request) {
    try {
      // 获取用户选择的语言 cn/en
      String lang = httpRequest.getHeader("lang");
      if (StringUtils.isBlank(lang)) {
        lang = "en-US";
      }
      return tAccountTradeService.queryByPage(request, lang);
    } catch (Exception e) {
      log.error("accountId:{}, 根据token获取用户账户交易信息失败,具体失败信息:{}", request.getAccountId(), e.getMessage(), e);
      return ResponseEntity.failure(HttpStatus.SC_INTERNAL_SERVER_ERROR, "获取用户账户交易信息,请联系管理员!");
    }
  }


  /**
   * 交易类型的展示
   * @return
   */
  @PostMapping("/displayTransactionTypes")
  public R displayTransactionTypes(HttpServletRequest httpRequest) {
    try {
      // 获取用户选择的语言 cn/en
      String lang = httpRequest.getHeader("lang");
      if (StringUtils.isBlank(lang)) {
        lang = "en-US";
      }
      return R.ok().put("list",tAccountTradeService.displayTransactionTypes(lang));
    } catch (Exception e) {
      log.error("根据token获取用户账户交易信息失败,具体失败信息:{}", e.getMessage(), e);
      return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "获取用户账户交易信息,请联系管理员!");
    }
  }


}
