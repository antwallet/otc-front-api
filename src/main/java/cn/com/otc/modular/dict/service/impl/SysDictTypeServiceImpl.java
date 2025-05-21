package cn.com.otc.modular.dict.service.impl;

import cn.com.otc.modular.dict.entity.pojo.SysDictData;
import cn.com.otc.modular.dict.entity.pojo.SysDictType;
import cn.com.otc.modular.dict.dao.SysDictTypeDao;
import cn.com.otc.modular.dict.entity.vo.SysDicResultVO;
import cn.com.otc.modular.dict.service.SysDictDataService;
import cn.com.otc.modular.dict.service.SysDictTypeService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.otc.common.utils.PageUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import cn.com.otc.common.utils.Query;


import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 字典类型表 服务实现类
 * </p>
 *
 * @author aimi
 * @since 2022-06-08
 */
@Service
@DS("master")
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeDao, SysDictType> implements SysDictTypeService {

    @Autowired
    private SysDictDataService sysDictDataService;

    public Map<String, List<SysDictData>> sysDictCache;

    public Map<SysDictType, List<SysDictData>> sysDictDateCache;

    @PostConstruct
    public void initSysDictCache() {
        Map<String, List<SysDictData>> sysDictCacheMap = new HashMap<>();
        Map<SysDictType, List<SysDictData>> sysDictDateCacheMap = new HashMap<>();
        QueryWrapper<SysDictType> queryWrapper = new QueryWrapper();
        queryWrapper.eq("status",0);
        List<SysDictType> list = this.baseMapper.selectList(queryWrapper);
        if(list ==null || list.size() ==0){
            return ;
        }
        for (SysDictType sysDictType:list) {
            QueryWrapper<SysDictData> queryWrapper_ = new QueryWrapper();
            queryWrapper_.eq("status",0);
            queryWrapper_.eq("dict_type",sysDictType.getDictType());
            List<SysDictData> sysDictDataList = sysDictDataService.list(queryWrapper_);
            sysDictCacheMap.put(sysDictType.getDictType(),sysDictDataList);
            sysDictDateCacheMap.put(sysDictType,sysDictDataList);
        }
        this.sysDictCache =  sysDictCacheMap;
        this.sysDictDateCache = sysDictDateCacheMap;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<SysDictType> queryWrapper = new QueryWrapper<>();
        if(params.get("dictName")!=null &&!"".equals(params.get("dictName").toString())){
            queryWrapper.like("dict_name",params.get("dictName").toString());
        }
        if(params.get("dictType")!=null &&!"".equals(params.get("dictType").toString())){
            queryWrapper.like("dict_type",params.get("dictType").toString());
        }
        if(params.get("status")!=null &&!"".equals(params.get("status").toString())){
            queryWrapper.eq("status",params.get("status").toString());
        }
        IPage<SysDictType> page = this.page(
        new Query<SysDictType>().getPage(params),
        queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<SysDicResultVO> queryAllDicType() {

        if(sysDictDateCache ==null || sysDictDateCache.size() ==0){
            return null;
        }

        List<SysDicResultVO> list = new ArrayList<>();
        for (Map.Entry<SysDictType, List<SysDictData>> entry:sysDictDateCache.entrySet()) {
            SysDicResultVO vo =new SysDicResultVO();
            vo.setSysDictType(entry.getKey());
            vo.setSysDictDataList(entry.getValue());
            list.add(vo);
        }

        return list;
    }

    @Override
    public List<SysDictData> getSysDictData(String dictType) {
        if(sysDictCache ==null || sysDictCache.size() ==0){
           return null;
        }
        return sysDictCache.get(dictType);
    }

    @Override
    public List<String> getSysDictDataValue(String dictType) {
        if(sysDictCache ==null || sysDictCache.size() ==0){
            return null;
        }
        List<SysDictData> list = sysDictCache.get(dictType);
        if(list ==null || list.size() == 0){
            return null;
        }

        List<String> sysDictDataValueList = new ArrayList<>();

        for (SysDictData sysDictData:list) {
            sysDictDataValueList.add(sysDictData.getDictValue());
        }
        return sysDictDataValueList;
    }



    @Override
    public void syncSysDictDataCache() {
        initSysDictCache();
    }
}
