package cn.com.otc.modular.sys.service;

import cn.com.otc.common.utils.PageUtils;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.sys.bean.pojo.TCharge;
import com.baomidou.mybatisplus.extension.service.IService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 充值表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-04-03
 */
public interface TChargeService extends IService<TCharge> {
        List<TCharge> getCurrChargeDataList(Integer status);

        TCharge queryCharge(String orderId,String userId, String base58CheckAddress,Integer chargeType,String chargeMoney, Integer status);

        void createCharge(String orderId,String userId,String base58CheckAddress,Integer chargeType,String chargeMoney,String qrcodeImage);

        void modifyChargeStatus(Long id,Integer status,String money,Integer chargeType,
            LocalDateTime chargeTime,String chargeTxid);

}
