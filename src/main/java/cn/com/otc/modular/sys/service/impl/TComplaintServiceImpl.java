package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.utils.I18nUtil;
import cn.com.otc.common.utils.UidGeneratorUtil;
import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TComplaint;
import cn.com.otc.modular.sys.bean.vo.TComplaintVo;
import cn.com.otc.modular.sys.dao.TComplaintDao;
import cn.com.otc.modular.sys.service.TComplaintService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


/**
 * <p>
 * 申述表 服务实现类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-06-19
 */
@Service
public class TComplaintServiceImpl extends ServiceImpl<TComplaintDao, TComplaint> implements
        TComplaintService {

    private static final Logger log = LoggerFactory.getLogger(TComplaintService.class);


    @Override
    public void createComplaintRecord(TComplaintVo tComplaintVo, String lang, UserInfoResult userInfoResult) {
        if (StringUtils.isBlank(tComplaintVo.getPaymentId()) || StringUtils.isBlank(tComplaintVo.getContent())) {
            log.error("createComplaintRecord 参数异常,请联系管理员,paymentId={},content={},userId={}", tComplaintVo.getPaymentId(), tComplaintVo.getContent(), userInfoResult.getUserTGID());
            throw new RRException(I18nUtil.getMessage("1017", lang), ResultCodeEnum.ILLEGAL_PARAMETER.code);
        }
        LocalDateTime now = LocalDateTime.now();
        String complaintId = "complaint".concat(UidGeneratorUtil.genId());//根据雪花算法获取账户id
        TComplaint tComplaint = new TComplaint();
        tComplaint.setComplaintId(complaintId);
        tComplaint.setUserId(userInfoResult.getUserTGID());
        tComplaint.setPaymentId(tComplaintVo.getPaymentId());
        if (StringUtils.isNotBlank(tComplaintVo.getImageUrl())){
            tComplaint.setImageUrl(tComplaintVo.getImageUrl());
        }
        tComplaint.setStatus("1");
        tComplaint.setContent(tComplaintVo.getContent());
        tComplaint.setCreateTime(now);
        this.save(tComplaint);
    }

}
