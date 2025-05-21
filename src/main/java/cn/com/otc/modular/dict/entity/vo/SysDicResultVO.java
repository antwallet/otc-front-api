package cn.com.otc.modular.dict.entity.vo;

import cn.com.otc.modular.dict.entity.pojo.SysDictData;
import cn.com.otc.modular.dict.entity.pojo.SysDictType;
import lombok.Data;

import java.util.List;

/**
 * @description:前端字典数据展示
 * @author: zhangliyan
 * @time: 2022/6/8
 */
@Data
public class SysDicResultVO {
    private SysDictType sysDictType;
    private List<SysDictData> sysDictDataList;
}
