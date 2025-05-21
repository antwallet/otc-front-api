package cn.com.otc.modular.sys.dao;

import cn.com.otc.modular.sys.bean.pojo.TPaymentRecords;
import cn.com.otc.modular.sys.bean.vo.TPaymentRecordsVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 付款记录表 Mapper 接口
 * </p>
 *
 * @author zhangliyan
 * @since 2024-03-26
 */
public interface TPaymentRecordsDao extends BaseMapper<TPaymentRecords> {

    List<TPaymentRecords>  selectTPaymentRecords(@Param("tronTransIds") List<String> tronTransIds, @Param("userTGID") String userTGID);


    List<TPaymentRecords> tPaymentRecordsList(@Param("tPaymentRecordsVo") TPaymentRecordsVo tPaymentRecordsVo);

    String getPaymentRecords(@Param("paymentId") String paymentId);

    List<TPaymentRecords> getPaymentRecord();

    Integer queryByPageCount(@Param("tronTransId") String tronTransId);

}
