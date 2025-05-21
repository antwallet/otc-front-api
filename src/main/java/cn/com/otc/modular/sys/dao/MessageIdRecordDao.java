package cn.com.otc.modular.sys.dao;

import cn.com.otc.modular.sys.bean.pojo.TMessageIdRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 红包确认记录 Mapper 接口
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
public interface MessageIdRecordDao  {

    /**
     *
     * 功能描述: 创建一个新的记录
     *
     * @auther: 2024
     * @date: 2024/7/17 下午8:33
     */
    void createMessageIdRecord(@Param("redpacketId") String redpacketId, @Param("messageId") String messageId,@Param("shareUserTgId")String shareUserId);

    /**
     *
     * 功能描述: 查询数据
     *
     * @auther: 2024
     * @date: 2024/7/17 下午8:44
     */
    List<TMessageIdRecord> selectMessageIdRecord(String redpacketId);

    /**
     *
     * 功能描述: 过期后删除他的messageId
     *
     * @auther: 2024
     * @date: 2024/7/22 19:21
     */
    void delectMessageId(String redpacketId);

    List<TMessageIdRecord>  selectMessageIdRecordList(@Param("newredpacketId") String newredpacketId, @Param("userTGID") String userTGID);

    void updateMessageIdRecord(@Param("messageId") String messageId, @Param("id") Long id);
}
