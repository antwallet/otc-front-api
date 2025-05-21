package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.utils.HttpStatus;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.sys.bean.pojo.TWalletPool;
import cn.com.otc.modular.tron.dto.bean.result.TWalletPoolResult;
import cn.com.otc.modular.sys.service.TWalletPoolService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/18
 */
@Slf4j
@RestController
@RequestMapping("/api/front/walletpool")
public class TWalletPoolController {
  @Autowired
  private TWalletPoolService tWalletPoolService;

  /**
   * 获取钱包地址池
   * @return
   */
  @RequestMapping("/list")
  public R list(){
    try{
      LambdaQueryWrapper<TWalletPool> lambdaQueryWrapper = new LambdaQueryWrapper<>();
      lambdaQueryWrapper.eq(TWalletPool::getIsdel,0);
      lambdaQueryWrapper.eq(TWalletPool::getIslock,0);
      lambdaQueryWrapper.orderByDesc(TWalletPool::getCreateTime);
      List<TWalletPool> tWalletPoolList = tWalletPoolService.list(lambdaQueryWrapper);
      List<TWalletPoolResult> list = new ArrayList<>();
      String[][] tWalletPool_str = new String[1][tWalletPoolList.size()];
      for (int i = 0; i < tWalletPoolList.size(); i++) {
        tWalletPool_str[0][i] =tWalletPoolList.get(i).getBase58CheckAddress();
        TWalletPoolResult result = new TWalletPoolResult();
        result.setId(tWalletPoolList.get(i).getId());
        result.setName(tWalletPoolList.get(i).getBase58CheckAddress());
        list.add(result);
      }
      return R.ok().put("list",list).put("tWalletPool_str",tWalletPool_str);
    }catch (Exception e){
      log.error("获取钱包地址池失败,具体失败信息:",e);
      return R.error(HttpStatus.SC_INTERNAL_SERVER_ERROR,"获取钱包地址池失败,请联系管理员!");
    }
  }

}
