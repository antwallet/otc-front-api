package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.common.response.ResponseEntity;
import cn.com.otc.common.utils.*;
import cn.com.otc.modular.dict.entity.pojo.SysDictData;
import cn.com.otc.modular.dict.service.SysDictDataService;
import cn.com.otc.modular.sys.bean.vo.AccountTradeByPageVO;
import cn.com.otc.modular.sys.bean.result.TAccountTradeResult;
import cn.com.otc.modular.sys.bean.result.QueryAccountTradeByPageRequest;
import cn.com.otc.modular.sys.bean.pojo.TAccountTrade;
import cn.com.otc.modular.sys.bean.pojo.TUser;
import cn.com.otc.modular.sys.dao.TAccountTradeDao;
import cn.com.otc.modular.sys.service.TAccountTradeService;
import cn.com.otc.modular.sys.service.TPaymentRecordsService;
import cn.com.otc.modular.sys.service.TUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 账户交易表 服务实现类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
@Slf4j
@Service
public class TAccountTradeServiceImpl extends ServiceImpl<TAccountTradeDao, TAccountTrade> implements TAccountTradeService {

    private static final String TRADE_TYPE = "trade_type";
    private static final String TRADE_TYPE_SEND_REDPACKET = "发红包";
    private static final String TRADE_TYPE_RECEIVE_REDPACKET = "收红包";
    @Autowired
    private TUserService tUserService;
    @Autowired
    private SysDictDataService sysDictDataService;
    @Autowired
    private TPaymentRecordsService tPaymentRecordsService;
    @Autowired
    private CheckTokenUtil checkTokenUtil;
    private static final String ACCOUNT_TYPE = "account_type";//账户类型

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<TAccountTrade> queryWrapper = new QueryWrapper<>();
        IPage<TAccountTrade> page = this.page(
                new Query<TAccountTrade>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public TAccountTrade selectAccountTradeByUserId(String userId, Integer tradeType) {
        LambdaQueryWrapper<TAccountTrade> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TAccountTrade::getUserId, userId);
        if (tradeType != null) {
            lambdaQueryWrapper.eq(TAccountTrade::getTradeType, tradeType);
        }
        lambdaQueryWrapper.last(" limit 1");
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    public BigDecimal getAccountTradeByUserId(String userId, String tradeNo) {
        LambdaQueryWrapper<TAccountTrade> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TAccountTrade::getUserId, userId)
                .eq(TAccountTrade::getTradeNo, tradeNo)
                .eq(TAccountTrade::getTradeType, 4)
                .orderByDesc(TAccountTrade::getCreateTime)
                .last(" limit 1");
        log.info("getAccountTradeByUserId  查询用户的流水金额:TAccountTrade:{}", this.getOne(lambdaQueryWrapper));
        if (this.getOne(lambdaQueryWrapper) == null) {
            return BigDecimal.ZERO;
        }
        String money = this.getOne(lambdaQueryWrapper).getMoney();
        return money != null && money.startsWith("+") ? new BigDecimal(money.substring(1)) : BigDecimal.ZERO;
    }

    @Override
    public void saveAccountTrade(String accountId, String userId, String tradeNo,
                                 String money, Integer tradeType, String leftAmount, Integer accountType, Integer receiveNum) {
        log.info("开始保存交易记录,accountId={},userId={},tradeNo={},money={},tradeType={},leftAmount={},accountType={}"
                , accountId, userId, tradeNo, money, tradeType, leftAmount, accountType);
        TAccountTrade tAccountTrade = new TAccountTrade();
        tAccountTrade.setAccountId(accountId);
        tAccountTrade.setUserId(userId);
        tAccountTrade.setTradeNo(tradeNo);
        tAccountTrade.setMoney(money);
        tAccountTrade.setTradeType(tradeType);
        tAccountTrade.setLeftAmount(leftAmount);
        tAccountTrade.setAccountType(accountType);
        tAccountTrade.setCreateTime(LocalDateTime.now());
        if (null != receiveNum) {
            tAccountTrade.setFullOpenCount(receiveNum);
        }
        this.save(tAccountTrade);
        log.info("结束保存交易记录,accountId={},userId={},tradeNo={},money={},tradeType={},leftAmount={}"
                , accountId, userId, tradeNo, money, tradeType, leftAmount);
    }

    @Override
    public void saveAccountTradeCharge(String accountId, String userId, String tradeNo,
                                       String money, Integer tradeType, String leftAmount, Integer accountType) {
        log.info("开始保存交易记录,accountId={},userId={},tradeNo={},money={},tradeType={},leftAmount={},accountType={}"
                , accountId, userId, tradeNo, money, tradeType, leftAmount, accountType);
        TAccountTrade tAccountTrade = new TAccountTrade();
        tAccountTrade.setAccountId(accountId);
        tAccountTrade.setUserId(userId);
        tAccountTrade.setTradeNo(tradeNo);
        tAccountTrade.setMoney(money);
        tAccountTrade.setTradeType(tradeType);
        tAccountTrade.setLeftAmount(leftAmount);
        tAccountTrade.setAccountType(accountType);
        tAccountTrade.setCreateTime(LocalDateTime.now());
        this.save(tAccountTrade);
        log.info("结束保存交易记录,accountId={},userId={},tradeNo={},money={},tradeType={},leftAmount={},accountType={}"
                , accountId, userId, tradeNo, money, tradeType, leftAmount, accountType);
    }

    @Override
    public ResponseEntity<?> queryByPage(QueryAccountTradeByPageRequest request, String lang) {

        try {
            AccountTradeByPageVO pageVO = new AccountTradeByPageVO();
            List<TAccountTrade> tAccountTrades;
            int start = (request.getPageIndex() - 1) * request.getPageSize();
            String accountId = request.getAccountId();
            Integer accountType = request.getAccountType();
            tAccountTrades = this.baseMapper.queryByPage(start, request.getPageSize(), accountId, accountType, request.getTradeType());
            Integer count = this.baseMapper.queryByPageCount(accountId, accountType);

            if (CollectionUtils.isEmpty(tAccountTrades)) {
                return ResponseEntity.success(I18nUtil.getMessage("608", lang));
            }
            /**
             * 2、返回交易记录数据到前端
             */
            List<TAccountTradeResult> list = new ArrayList<>();
            TUser tUser = tUserService.getTUserByUserId(tAccountTrades.get(0).getUserId());
            for (TAccountTrade tAccountTrade : tAccountTrades) {
                TAccountTradeResult result = new TAccountTradeResult();
                result.setMoney(tAccountTrade.getMoney());
                String accountTypeInfo = sysDictDataService.getDictLabel(ACCOUNT_TYPE, accountType.toString());
                result.setAccountTypeInfo(accountTypeInfo);
                if (tUser == null) {
                    result.setTradeTypeInfo(sysDictDataService.getDictLabelEn(TRADE_TYPE, tAccountTrade.getTradeType().toString()));
                    if (lang.equals("zh-CN")) { //判断是英文还是中文
                        result.setTradeTypeInfo(sysDictDataService.getDictLabel(TRADE_TYPE, tAccountTrade.getTradeType().toString()));
                    }
                } else {
                    result.setTradeTypeInfo(tUser.getName().concat("-").concat(sysDictDataService.getDictLabelEn(TRADE_TYPE, tAccountTrade.getTradeType().toString())));
                    if (lang.equals("zh-CN")) { // 判断是英文或者中文
                        result.setTradeTypeInfo(tUser.getName().concat("-").concat(sysDictDataService.getDictLabel(TRADE_TYPE, tAccountTrade.getTradeType().toString())));
                    }
                }
                if (23 == tAccountTrade.getTradeType()) {
                    // 状态复制
                    String paymentRecords = tPaymentRecordsService.getPaymentRecords(tAccountTrade.getTradeNo());
                    if (StringUtils.isNotBlank(paymentRecords)){
                        result.setState(paymentRecords);
                    }

                }
                result.setCreateTime(tAccountTrade.getCreateTime().atZone(ZoneId.of("Asia/Shanghai")).format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                result.setExchangeMoney(tAccountTrade.getMoney().substring(1).concat(accountTypeInfo));
                result.setTradeNo(tAccountTrade.getTradeNo());
                list.add(result);
            }
            pageVO.setList(list);
            pageVO.setTotalCount(count);
            return ResponseEntity.success("查询成功", pageVO);
        } catch (Exception e) {
            log.error("TAccountTradeServiceImpl--queryByPage查询异常：{}", e.getMessage(), e);
            return ResponseEntity.failure("查询异常");
        }
    }

    @Override
    public List<TAccountTrade> selectAccountTradeList() {
        LambdaQueryWrapper<TAccountTrade> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TAccountTrade::getTradeType, 4);
        lambdaQueryWrapper.like(TAccountTrade::getTradeNo, "share_redpacket");

        List<TAccountTrade> list = this.list(lambdaQueryWrapper);
        return list;
    }

    @Override
    public List<SysDictData> displayTransactionTypes(String lang) {
        return sysDictDataService.getSysDictData(TRADE_TYPE);
    }

}
