package cn.com.otc.modular.dict.service;

import cn.com.otc.modular.dict.entity.pojo.SysDictData;
import cn.com.otc.modular.dict.entity.pojo.SysDictType;
import cn.com.otc.modular.dict.entity.vo.SysDicResultVO;
import com.baomidou.mybatisplus.extension.service.IService;

import cn.com.otc.common.utils.PageUtils;


import java.util.List;
import java.util.Map;

/**
 * <p>
 * 字典类型表 服务类
 * </p>
 *
 * @author aimi
 * @since 2022-06-08
 */
public interface SysDictTypeService extends IService<SysDictType> {
        PageUtils queryPage(Map<String, Object> params);

        List<SysDicResultVO> queryAllDicType();

        List<SysDictData> getSysDictData(String dictType);

        List<String> getSysDictDataValue(String dictType);

        void syncSysDictDataCache();
}
