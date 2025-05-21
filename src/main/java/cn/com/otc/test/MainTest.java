package cn.com.otc.test;

import cn.com.otc.OtcFrontApiApplication;
import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.modular.tron.dto.bean.WithdrawUSDTData;
import cn.com.otc.modular.tron.util.TronUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.crypto.SECP256K1;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {OtcFrontApiApplication.class})
@Slf4j
public class MainTest {
  @Autowired private TronUtils tronApiService;
  @Autowired private MyCommonConfig myCommonConfig;;
  public static String toAddress = "TXoDw9PUuBWwp2GmiDvFwdXfja6KREpkyY";

  /** 获取trx,usdt余额 */
  @Test
  public void balanceOf() {
    BigDecimal trxBalanceOf =
        tronApiService.getAccountTrxBalance(myCommonConfig.getHexPrivateKey(), toAddress);
    BigDecimal usdtBalanceOf =
        tronApiService.getAccountUSDTBalance(myCommonConfig.getHexPrivateKey(), toAddress);
    log.info("trx 余额:{}", trxBalanceOf);
    log.info("usdt余额:{}", usdtBalanceOf);
  }

  /** 转账TRX */
  @Test
  @SneakyThrows
  public void trunsferTRX() {
    String txid =
        tronApiService.trunsferTRX(
            myCommonConfig.getHexPrivateKey(), "", toAddress, 1000000L);
    log.info("交易ID:{}", txid);
    Thread.sleep(5000);
    String status = tronApiService.getTransactionStatusById(myCommonConfig.getHexPrivateKey(),txid);
    log.info("交易状态:{}", status);
  }

  /** 转账USDT */
  @Test
  @SneakyThrows
  public void trunsferUSDT() {
    WithdrawUSDTData withdrawUSDTData =
        tronApiService.trunsferUSDT(
            myCommonConfig.getWithdrawHexPrivateKey(),
            myCommonConfig.getWithdrawAddress(),
            "THUACE6wwjA3jfDKy3edoCCsc7ayMMwNUs",new BigInteger("1000000").multiply(new BigInteger("1")));
    log.info("交易ID:{}", withdrawUSDTData.getTxid());
    Thread.sleep(5000);
    String status = tronApiService.getTransactionStatusById(myCommonConfig.getWithdrawHexPrivateKey(), withdrawUSDTData.getTxid());
    log.info("交易状态:{}", status);
  }

  /** 转账USDT */
  @Test
  @SneakyThrows
  public void getTransactionStatusById() {
    String txid = "9f4aab03159fda63cd9252929dc4ab4acc00c3ba048f79d11f6132e6d59d8d02";
    String status = tronApiService.getTransactionStatusById(myCommonConfig.getWithdrawHexPrivateKey(),txid);
    log.info("交易状态:{}", status);
  }

  /** 私钥转钱包地址 */
  @Test
  @SneakyThrows
  public void privateKeyToAddress() {
    byte[] rawAddr =
        KeyPair.publicKeyToAddress(
            SECP256K1.PublicKey.create(SECP256K1.PrivateKey.create(myCommonConfig.getHexPrivateKey())));
    String address = Hex.toHexString(rawAddr);
    log.info("地址:{}", address);
  }
}
