package cn.com.otc.modular.tron.service;

import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TTronCollectRecord;
import cn.com.otc.modular.tron.dto.bean.result.CollectTronResult;
import cn.com.otc.modular.tron.dto.bean.vo.TronWithdrawMoneyVO;
import cn.com.otc.modular.tron.dto.bean.vo.UserBuyPremiumVO;
import cn.com.otc.modular.tron.dto.vo.result.TronTransResult;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/2/28
 */
public interface TronManageService {

  /**
   * 获取钱包地址生成的二维码
   *
   * @param userTGID
   * @param lang
   * @return
   */
  ResponseEntity<?> getWalletQrCode(String userTGID, String lang) ;

  /**
   * 生成钱包地址生成的二维码
   *
   * @param userTGID
   * @param accountType
   * @param money
   * @param lang
   * @return
   */
  ResponseEntity<?> createWalletQrCode(String userTGID, String accountType, String money, String lang);


  /**
   * 取消充值订单
   *
   * @param userTGID
   * @param orderId
   * @param lang
   */
  void cancelTronCharge(String userTGID, String orderId, String lang);

   /**
    * 充值
    */
  void handleTronCharge();

  /**
   * 创建收款
   *
   * @param transAccountVO
   * @param lang
   * @return
   */
  //String createOTronTrans(TransAccountVO transAccountVO, UserInfoResult userInfoResult, String lang);

   /**
    * 收款
    */
   //void handleTronTransAccount(String tronTransId, UserInfoResult userInfoResult, String lang);

   /**
    * 提现申请
    */
   ResponseEntity<?> applyTronWithdrawMoney(TronWithdrawMoneyVO tronWithdrawMoneyVO, UserInfoResult userInfoResult, String lang);

  /**
   * 处理用户提现手续费统计
   */
  //void handleUserWithdrawalTj();

  /**
   * 用户购买会员-手动
   */
  ResponseEntity<?> handleUserBuyPremiumBySelf(UserBuyPremiumVO userBuyPremiumVO, UserInfoResult userInfoResult, String lang);

  /**
   * 用户购买会员-自动
   */
  ResponseEntity<?> handleUserBuyPremiumByAuto(UserBuyPremiumVO userBuyPremiumVO, UserInfoResult userInfoResult, String lang);

  /**
   * 一键归集区块链余额
   */
  void handleCollectTronAmount();

  /**
   * 刷新转账的状态
   */
  void handleRefreshCollectTronStatus(String taskId, String collectTime, String lang);

  /**
   * 获取归集记录
   */

  List<CollectTronResult> getCollectTronResultList();

  /**
   * 获取归集详细记录
   */
  List<TTronCollectRecord> getCollectTronRecordDetails(String taskId, HttpServletRequest httpRequest);

  /**
   * 自动提现
   */
  void handleWithdrawAuto(Long id,String toAddress,String blockchainType,String withdrawType,String withdrawMoney, HttpServletRequest httpRequest);

  /**
   *刷新自动提现的状态
   */
  void refreshWithdrawStatus(Long id, HttpServletRequest httpRequest);

  /**
   * 获取收款订单的信息
   */
  TronTransResult getTronTransAccount(String tronTransId, UserInfoResult userInfoResult, String lang);

  /**
   * 用户付款
   */
  R handleUserPayment(String tronTransId, String userTGID, String lang, String shareUserId);
}
