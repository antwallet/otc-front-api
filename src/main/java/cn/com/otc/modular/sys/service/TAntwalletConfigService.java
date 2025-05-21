package cn.com.otc.modular.sys.service;

import cn.com.otc.common.utils.PageUtils;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.sys.bean.pojo.TAntwalletConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 配置表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-04-12
 */
public interface TAntwalletConfigService extends IService<TAntwalletConfig> {

    TAntwalletConfig selectTAntwalletbotConfigByPKey(String redEnvelopeAmountRange);

}
