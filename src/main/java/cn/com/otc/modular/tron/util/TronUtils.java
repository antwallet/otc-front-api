package cn.com.otc.modular.tron.util;

import cn.com.otc.common.config.MyCommonConfig;
import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.modular.tron.dto.bean.*;
import cn.com.otc.modular.tron.dto.response.TronResponse;
import cn.com.otc.modular.tron.dto.response.TronTRXResponse;
import cn.com.otc.modular.tron.dto.response.TronUSDTResponse;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.trident.abi.FunctionReturnDecoder;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.Bool;
import org.tron.trident.abi.datatypes.Function;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.core.transaction.TransactionBuilder;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Numeric;

@Slf4j
@Component
public class TronUtils {

    @Autowired
    private MyCommonConfig myCommonConfig;


    private static final String SHASTA_HOST = "https://api.shasta.trongrid.io/v1/accounts/%s/transactions?limit=1&only_to=true";
    public static final String GET_TRANSACTIONS_TRX = "https://apilist.tronscanapi.com/api/trx/transfer?sort=-timestamp&count=true&start=0&start_timestamp=%s&end_timestamp=%s&address=%s&filterTokenValue=0";//TRX的交易查询
    public static final String GET_TRANSACTIONS_USDT = "https://apilist.tronscanapi.com/api/filter/trc20/transfers?start=0&start_timestamp=%s&end_timestamp=%s&sort=-timestamp&count=true&filterTokenValue=0&relatedAddress=%s";//USDT的交易查询
    public static final String GET_ACCOUNT_INFO = "https://apilist.tronscanapi.com/api/accountv2?address=%s";

    // 对用户生成钱包
    public WalletBean createWallet(String userId) {
        log.info("开始为用户 userId={} 生成TRC20钱包地址",userId);
        KeyPair keyPair = ApiWrapper.generateAddress();

        WalletBean walletBean = new WalletBean();
        walletBean.setPrivateKey(keyPair.toPrivateKey());
        walletBean.setPublicKey(keyPair.toPublicKey());
        walletBean.setBase58CheckAddress(keyPair.toBase58CheckAddress());
        walletBean.setHexAddress(keyPair.toHexAddress());

        log.info("结束生成TRC20钱包地址，当前用户 userId={}的钱包地址 address={}",userId,walletBean.getBase58CheckAddress());
        return walletBean;
    }

    // 获取用户余额
    public long getBalance(String hexPrivateKey, String apiKey, String address) {
        ApiWrapper wrapper = ApiWrapper.ofShasta(hexPrivateKey);
        // 正式环境改成下面的，并传输apiKey
//        ApiWrapper wrapper = ApiWrapper.ofMainnet("hex private key", "API key");
        return wrapper.getAccountBalance(address);
    }

    // 获取TRX交易记录
    public TronResponse<TransferData> getTransferRecordByAddress(String address) {
        HttpRequest httpRequest = HttpUtil.createGet(String.format(SHASTA_HOST, address));
        httpRequest.header("accept", "application/json");
        HttpResponse httpResponse;
        try {
            httpResponse = httpRequest.execute();
        } catch (Exception e) {
            return null;
        }
        if (!httpResponse.isOk()) {
            return null;
        }
        return JSONUtil.toBean(httpResponse.body(), new TypeReference<TronResponse<TransferData>>() {
        },false);
    }

    // 获取TRX交易记录
    public TronTRXResponse<TransferTRXData> getTransferTRXRecordByAddress(String address,Long start_timestamp,Long end_timestamp) {
        HttpRequest httpRequest = HttpUtil.createGet(String.format(GET_TRANSACTIONS_TRX,start_timestamp,end_timestamp, address));
        httpRequest.header("accept", "application/json");
        HttpResponse httpResponse;
        try {
            httpResponse = httpRequest.execute();
        } catch (Exception e) {
            return null;
        }
        if (!httpResponse.isOk()) {
            return null;
        }
        return JSONUtil.toBean(httpResponse.body(), new TypeReference<TronTRXResponse<TransferTRXData>>() {
        },false);
    }

    // 获取TRX交易记录
    public TronUSDTResponse<TransferUSDTData> getTransferUSDTRecordByAddress(String address,Long start_timestamp,Long end_timestamp) {
        HttpRequest httpRequest = HttpUtil.createGet(String.format(GET_TRANSACTIONS_USDT,start_timestamp,end_timestamp, address));
        httpRequest.header("accept", "application/json");
        HttpResponse httpResponse;
        try {
            httpResponse = httpRequest.execute();
        } catch (Exception e) {
            return null;
        }
        if (!httpResponse.isOk()) {
            return null;
        }
        return JSONUtil.toBean(httpResponse.body(), new TypeReference<TronUSDTResponse<TransferUSDTData>>() {
        },false);
    }

    // 发起转账
    public static String doTransAccount(ApiWrapper user, String from, String to, long amount, long timeOut) {
        log.info("start trans from=" + from + ";to=" + to + ";amount=" + amount);
        String transactionId;
        try {
            Response.TransactionExtention transactionExtention = user.transfer(from, to, amount);
            Chain.Transaction signedTransaction = user.signTransaction(transactionExtention);
            transactionId = user.broadcastTransaction(signedTransaction);
            System.out.println("transactionId=" + transactionId);
        } catch (Exception e) {
            log.error("transfer e=" + e);
            return null;
        }
        if (transactionId != null) {
            for (int i = 0; i < timeOut; i++) {
                try {
                    Response.TransactionInfo transactionInfo = user.getTransactionInfoById(transactionId);
                    if (transactionInfo.hasReceipt()) {
                        return transactionId;
                    }
                } catch (Exception e) {
                    log.error("transaction error.e=" + e);
                }
            }
        }
        return null;
    }


    public ApiWrapper getApiWrapper(String hexPrivateKey) {
        if (myCommonConfig.getTronDomainOnline()) {
            return ApiWrapper.ofMainnet(hexPrivateKey, myCommonConfig.getTronApiKey());
        } else {
            return ApiWrapper.ofShasta(hexPrivateKey);
        }
    }

    /**
     * 查询账户TRX余额
     *
     * @param address
     * @return
     */
    public BigDecimal getAccountTrxBalance(String hexPrivateKey, String address) {
        ApiWrapper client = getApiWrapper(hexPrivateKey);
        Response.Account account = client.getAccount(address);

        client.close();
        return new BigDecimal(account.getBalance());
    }

    /**
     * 获取USDT余额
     *
     * @param address
     * @return
     */
    public BigDecimal getAccountUSDTBalance(String hexPrivateKey, String address) {
        ApiWrapper client = getApiWrapper(hexPrivateKey);
        Function balanceOf =
            new Function(
                "balanceOf",
                Arrays.asList(new Address(address)),
                Arrays.asList(new org.tron.trident.abi.TypeReference<Uint256>() {}));
        Response.TransactionExtention extension =
            client.constantCall(address, myCommonConfig.getTrc20Address(), balanceOf);

        String result = Numeric.toHexString(extension.getConstantResult(0).toByteArray());

        BigInteger value =
            (BigInteger)
                FunctionReturnDecoder.decode(result, balanceOf.getOutputParameters()).get(0).getValue();

        client.close();

        return new BigDecimal(value);
    }

    /**
     * 转账TRX
     *
     * @param hexPrivateKey
     * @param fromAddress
     * @param toAddress
     * @param amount
     * @return
     * @throws IllegalException
     */
    public String trunsferTRX(String hexPrivateKey, String fromAddress, String toAddress, long amount)
        throws IllegalException {
        ApiWrapper client = getApiWrapper(hexPrivateKey);

        // 构建交易
        Response.TransactionExtention transactionExtention = client.transfer(fromAddress, toAddress, amount);

        // 估算带宽
        long estimatedBandwidth = client.estimateBandwidth(transactionExtention.getTransaction());

        // 计算链上剩余宽带
        long remainingFreeNet = getAvailableBandwidth(hexPrivateKey,fromAddress);

        log.info("trunsferTRX estimatedBandwidth={},remainingFreeNet={}" , estimatedBandwidth,remainingFreeNet);

        //判断当前链上的宽带是否满足本次转账估算的宽带
        //if(!hasSufficientResources(0,0,estimatedBandwidth,remainingFreeNet)){
        if(remainingFreeNet < estimatedBandwidth){
            log.warn("TronUtils.trunsferTRX 宽带不满足本次转账交易 fromAddress={},toAddress={},amount={},estimatedBandwidth={},remainingFreeNet={}",
                fromAddress, toAddress, amount, estimatedBandwidth, remainingFreeNet);
            throw new RRException("宽带不满足本次转账TRX交易", ResultCodeEnum.TRUNSFER_TRX_NOT_ENOUGH.code);
        }

        Chain.Transaction transaction = client.signTransaction(transactionExtention);
        String txid = client.broadcastTransaction(transaction);
        client.close();
        return txid;
    }

    /**
     * 转账USDT
     *
     * @param hexPrivateKey
     * @param fromAddress
     * @param toAddress
     * @param amount
     * @return
     */
    public WithdrawUSDTData trunsferUSDT(
        String hexPrivateKey, String fromAddress, String toAddress, BigInteger amount) {
        ApiWrapper client = getApiWrapper(hexPrivateKey);

        // 构建调用智能合约的交易
        Function transfer = new Function("transfer", Arrays.asList(new Address(toAddress), new Uint256(amount)),
            Arrays.asList(new org.tron.trident.abi.TypeReference<Bool>() {}));

        // 使用 constantCall 模拟合约调用以估算能量消耗
        Response.TransactionExtention transactionExtention = client.constantCall(fromAddress, myCommonConfig.getTrc20Address(), transfer);

        // 获取估算宽带消耗
        long estimatedBandwidth = client.estimateBandwidth(transactionExtention.getTransaction());

        // 获取估算的1笔能量消耗
        long estimatedEnergy = transactionExtention.getEnergyUsed();

        // 计算剩余宽带
        long remainingFreeNet = getAvailableBandwidth(hexPrivateKey,fromAddress);

        // 计算剩余能量
        long remainingEnergy = getAvailableEnergy(hexPrivateKey,fromAddress);

        /*//获取被转账的账户的USDT值
        BigInteger usdtBalance = null;
        try {
            usdtBalance = getAccountUSDTBalanceByUrl(toAddress);
        }catch (Exception e){
            throw new RRException("trunsferUSDT 转账失败,查询账户是否有USDT失败", ResultCodeEnum.WITHDRAWAL_AUTO_ERROR.code);
        }

        if(usdtBalance == null){
            log.warn("trunsferUSDT address={}获取账户上的U为空", toAddress);
            throw new RRException("trunsferUSDT 转账失败,usdtBalance is null,请检查", ResultCodeEnum.WITHDRAWAL_AUTO_ERROR.code);
        }

        long needEstimatedEnergy;
        if(usdtBalance.compareTo(BigInteger.ZERO) <= 0){
            needEstimatedEnergy = estimatedEnergy * 2;
        }else{
            needEstimatedEnergy = estimatedEnergy;
        }*/

        //判断当前链上的宽带是否满足本次转账估算的宽带
        log.info("trunsferUSDT estimatedBandwidth={},estimatedEnergy={},remainingFreeNet={},remainingEnergy={}" ,
                estimatedBandwidth,estimatedEnergy,remainingFreeNet,remainingEnergy);
        Integer quantity = 0;
        long purchaseEnergy = estimatedEnergy;
        //判断当前链上的宽带是否满足本次转账估算的宽带
        if(!hasSufficientResources(estimatedEnergy,remainingEnergy,estimatedBandwidth,remainingFreeNet)){
            log.warn("TronUtils.trunsferUSDT 能量或者宽带不满足本次转账USDT交易 fromAddress={},toAddress={},amount={},estimatedBandwidth={},estimatedEnergy={},remainingFreeNet={},remainingEnergy={}",
                    fromAddress, toAddress, amount, estimatedBandwidth, estimatedEnergy, remainingFreeNet, remainingEnergy);
            //能量不足时需要购买能量
            purchaseEnergy = estimatedEnergy - remainingEnergy;
            log.info("TronUtils.trunsferUSDT 还缺少的能量 estimatedEnergy={},remainingEnergy={},purchaseEnergy={}", estimatedEnergy, remainingEnergy, purchaseEnergy);
            quantity = Integer.parseInt(String.valueOf(purchaseEnergy));
            log.info("TronUtils.trunsferUSDT 购买能量 quantity={}", quantity);
            String order = MeFreeUtil.order(myCommonConfig.getEnergyApiKey(), myCommonConfig.getEnergyApiSecret(),
                    1, quantity, fromAddress, 1);
            JSONObject jsonObject = new JSONObject(order);
            if (!jsonObject.get("code").equals(0)){
                log.info("TronUtils.trunsferUSDT 购买能量失败啦,order={}", order);
                throw new RRException("购买能量失败，不支持本次USDT交易", ResultCodeEnum.ENERGY_BUY_ERROR.code);
            }
            log.info("TronUtils.trunsferUSDT 购买能量成功 jsonObject:{}", jsonObject);
        }

        /**
         *  购买能量后，有可能延迟，导致链上的能量还没有加上，这个时候先查询一下当前能量有没有加上，没有加上需要延迟8秒中，等确定到账后再转账
         */
        // 计算剩余能量
        long remainingEnergy_after = getAvailableEnergy(hexPrivateKey,fromAddress);
        if(!hasSufficientResources(estimatedEnergy,remainingEnergy_after,estimatedBandwidth,remainingFreeNet)){
            log.warn("TronUtils.trunsferUSDT 购买的会员能量还没有到账 fromAddress={},toAddress={},amount={},estimatedBandwidth={},estimatedEnergy={},remainingFreeNet={},remainingEnergy={}",
                fromAddress, toAddress, amount, estimatedBandwidth,estimatedEnergy,remainingFreeNet,remainingEnergy_after);
            try {
                Thread.sleep(8000);
            }catch (Exception e){
                log.error("TronUtils.trunsferUSDT 转账暂停8秒失败，本次USDT交易失败,具体失败信息:",e);
                throw new RRException("TronUtils.trunsferUSDT 转账暂停8秒失败，本次USDT交易失败", ResultCodeEnum.ENERGY_BUY_ERROR.code);
            }

            // 计算剩余能量
            long remainingEnergy_after_again = getAvailableEnergy(hexPrivateKey,fromAddress);
            if(!hasSufficientResources(estimatedEnergy,remainingEnergy_after_again,estimatedBandwidth,remainingFreeNet)){
                log.info("TronUtils.trunsferUSDT 购买能量等8秒后一直没有到账,fromAddress={},toAddress={},amount={},estimatedBandwidth={},estimatedEnergy={},remainingFreeNet={},remainingEnergy={}",
                    fromAddress, toAddress, amount, estimatedBandwidth,estimatedEnergy,remainingFreeNet,remainingEnergy_after_again);
                throw new RRException("TronUtils.trunsferUSDT 购买能量等8秒后一直没有到账", ResultCodeEnum.ENERGY_BUY_ERROR.code);
            }
        }

        log.info("trunsferUSDT 最终消耗的能量: purchaseEnergy={}" , purchaseEnergy);

        // 设置合理的 feeLimit，加上一些余量
        long feeLimit = estimatedEnergy + 100;

        // 构建实际的合约调用交易
        TransactionBuilder builder = client.triggerCall(fromAddress, myCommonConfig.getTrc20Address(), transfer);
        builder.setFeeLimit(100000000);

        // 签名交易
        Chain.Transaction transaction = client.signTransaction(builder.getTransaction());
        //Chain.Transaction transaction = client.signTransaction(transactionExtention.getTransaction());

        // 广播交易
        String txid = client.broadcastTransaction(transaction);

        client.close();
        return new WithdrawUSDTData(String.valueOf(purchaseEnergy), txid);
    }

    /**
     * 查询交易状态
     *
     * @param txid
     * @return
     * @throws IllegalException
     */
    public String getTransactionStatusById(String hexPrivateKey,String txid) throws IllegalException {
        ApiWrapper client = getApiWrapper(hexPrivateKey);
        Chain.Transaction getTransaction = client.getTransactionById(txid);
        client.close();
        return getTransaction.getRet(0).getContractRet().name();
    }

    /**
     * 查询TRON账户资源信息
     *
     * @param address
     * @return
     * @throws IllegalException
     */
    public Response.AccountResourceMessage getAccountResource(String hexPrivateKey , String address) {
        ApiWrapper client = getApiWrapper(hexPrivateKey);
        Response.AccountResourceMessage accountResourceMessage = client.getAccountResource(address);
        client.close();
        return accountResourceMessage;
    }

    /**
     * 获取可用宽带
     * @param address
     * @return
     */
    public long getAvailableBandwidth(String hexPrivateKey, String address) {
        Response.AccountResourceMessage resource = getAccountResource(hexPrivateKey,address);
        long freeBandwidth = resource.getFreeNetLimit();
        long netBandwidth = resource.getNetLimit();
        long usedBandwidth = resource.getFreeNetUsed();
        return  netBandwidth - usedBandwidth + freeBandwidth;
    }

    /**
     * 获取可用能量
     * @param address
     * @return
     */
    public long getAvailableEnergy(String hexPrivateKey,String address) {
        Response.AccountResourceMessage resource = getAccountResource(hexPrivateKey,address);
        long energyLimit = resource.getEnergyLimit();
        long energyUsed = resource.getEnergyUsed();
        return energyLimit - energyUsed;
    }

    /**
     * 判断是否满足转账条件
     * @param energyRequired
     * @param bandwidthRequired
     * @return
     */
    public boolean hasSufficientResources(long energyRequired,long remainingEnergy, long bandwidthRequired,long remainingFreeNet) {

        // 检查资源是否满足需求
        //return remainingFreeNet >= bandwidthRequired && remainingEnergy >= energyRequired;
        return remainingEnergy >= energyRequired;
    }


    /**
     * 获取账户上的U
     * @return
     */
    public BigInteger getAccountUSDTBalanceByUrl(String address){
        try {
            HttpRequest httpRequest = HttpUtil.createGet(String.format(GET_ACCOUNT_INFO, address));
            httpRequest.header("accept", "application/json");
            HttpResponse httpResponse = httpRequest.execute();
            if (!httpResponse.isOk()) {
                log.warn("getAccountUSDTBalanceByUrl address={}获取账户上的U失败,result={}", address,
                    JSONUtil.toJsonStr(httpResponse));
                throw new RRException("get account usdt balance error",
                    ResultCodeEnum.ENERGY_BUY_ERROR.code);
            }

            String body = httpResponse.body();

            JSONObject jsonObject = JSONUtil.parseObj(body);

            // 获取 withPriceTokens 列表
            JSONArray withPriceTokens = jsonObject.getJSONArray("withPriceTokens");

            // 遍历列表并提取 balance 值
            for (Object tokenObj : withPriceTokens) {
                JSONObject tokenJson = (JSONObject) tokenObj;
                String tokenAbbr = tokenJson.getStr("tokenAbbr");
                if (tokenAbbr.equals("USDT")) {
                    BigInteger balance = new BigInteger(tokenJson.getStr("balance"));
                    return balance;
                }
            }
        }catch (Exception e){
            log.error(
                String.format("getAccountUSDTBalanceByUrl address={%s}获取账户上的U失败,", address), e);
            throw new RRException("get account usdt balance error",
                ResultCodeEnum.WITHDRAWAL_AUTO_ERROR.code);
        }
        return BigInteger.ZERO;
    }
}
