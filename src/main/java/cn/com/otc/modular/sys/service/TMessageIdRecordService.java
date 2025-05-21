package cn.com.otc.modular.sys.service;

import cn.com.otc.modular.sys.bean.pojo.TMessageIdRecord;
import java.util.List;

/**
 * <p>
 * 确认点击红包 服务类
 * </p>
 *
 * @author 2024
 * @since 2024-07-09
 */
public interface TMessageIdRecordService {


    /**
     *
     * 功能描述: 点击按钮后记录数据
     *
     * @auther: 2024
     * @date: 2024/7/17 下午8:32
     */
    void messageIdRecord(String messageId, String query,String shareUserId);

    /**
     *
     * 功能描述: 查询红包数据
     *
     * @auther: 2024
     * @date: 2024/7/17 下午8:43
     */
    List<TMessageIdRecord> selectMessageIdRecord(String redpacketId);

    /**
     *
     * 功能描述: 删除他的messageId
     *
     * @auther: 2024
     * @date: 2024/7/22 19:19
     */
    void delectMessageId(String redpacketId);

    List<TMessageIdRecord> selectMessageIdRecordList(String newredpacketId, String userTGID);

    /**
     * 功能描述: 修改他的messageId
     *
     * @auther: 2024
     * @date: 2024/8/29 20:47
     */
    void updateMessageIdRecord(String messageId, Long id);
}
