package cn.com.otc.modular.sys.service.impl;

import cn.com.otc.common.enums.ResultCodeEnum;
import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.redis.RedisOperate;
import cn.com.otc.common.utils.I18nUtil;
import cn.com.otc.modular.sys.bean.pojo.TMessageIdRecord;
import cn.com.otc.modular.sys.dao.MessageIdRecordDao;
import cn.com.otc.modular.sys.service.TMessageIdRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 确认点击红包 服务实现类
 * </p>
 *
 * @author 2024
 * @since 2024-07-09
 */
@Service
public class TMessageIdRecordServiceImpl  implements TMessageIdRecordService {
    private static final Logger log = LoggerFactory.getLogger(TMessageIdRecordService.class);

    @Autowired
    private MessageIdRecordDao messageIdRecordDao;
    @Autowired
    private RedisOperate redisOperate;
    @Override
    public void messageIdRecord(String messageId, String query,String shareUserId) {
        if (messageId==null || query==null || shareUserId == null){
            log.error("messageIdRecord 参数异常,请联系管理员,messageId={},query={},shareUserId={}",messageId,query,shareUserId);
            throw new RRException(I18nUtil.getMessage("1017",null), ResultCodeEnum.ILLEGAL_PARAMETER.code);
        }
        messageIdRecordDao.createMessageIdRecord(query, messageId,shareUserId);
    }

    @Override
    public List<TMessageIdRecord> selectMessageIdRecord(String redpacketId) {
        List<TMessageIdRecord> tMessageIdRecord = messageIdRecordDao.selectMessageIdRecord(redpacketId);
        return tMessageIdRecord;
    }

    @Override
    public void delectMessageId(String redpacketId) {
        messageIdRecordDao.delectMessageId(redpacketId);
    }

    @Override
    public List<TMessageIdRecord> selectMessageIdRecordList(String newredpacketId, String userTGID) {
        return  messageIdRecordDao.selectMessageIdRecordList(newredpacketId,userTGID);
    }

    @Override
    public void updateMessageIdRecord(String messageId, Long id) {
        messageIdRecordDao.updateMessageIdRecord(messageId,id);
    }
}