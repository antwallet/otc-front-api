package cn.com.otc.modular.dict.service;

import cn.com.otc.modular.dict.entity.pojo.SysDictData;
import com.baomidou.mybatisplus.extension.service.IService;

import cn.com.otc.common.utils.PageUtils;


import java.util.List;
import java.util.Map;

/**
 * <p>
 * 字典数据表 服务类
 * </p>
 *
 * @author aimi
 * @since 2022-06-08
 */
public interface SysDictDataService extends IService<SysDictData> {
    PageUtils queryPage(Map<String, Object> params);

    Map<String, Object> getSysDictByType(String dictType);

    String getDictLabel(String dictType, String dictValue);

    String getDictLabelEn(String dictType, String dictValue);

    String getDictValue(String dictType, String dictLable);

    String getDictValueEn(String dictType, String dictLableEn);


    List<SysDictData> getSysDictData(String tradeType);

}
