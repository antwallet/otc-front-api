package cn.com.otc.modular.dict.service.impl;

import cn.com.otc.common.utils.PageUtils;
import cn.com.otc.common.utils.Query;
import cn.com.otc.modular.dict.dao.SysDictDataDao;
import cn.com.otc.modular.dict.entity.pojo.SysDictData;
import cn.com.otc.modular.dict.service.SysDictDataService;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 字典数据表 服务实现类
 * </p>
 *
 * @author aimi
 * @since 2022-06-08
 */
@Slf4j
@Service
@DS("master")
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataDao, SysDictData> implements SysDictDataService {

    private Map<String,Map<String,Object>> sysDictCache = new HashMap<>();//字典数据的缓存

    /**
     * 初始化字典数据的缓存
     */
    @PostConstruct
    public void initSysDictData(){
        log.info("开始初始化字典数据缓存");
        try {
            Map<String,Map<String,Object>> sysDictCache = new HashMap<>();//字典数据的缓存
            Map<String, String> sysDictTypeMap = new HashMap<>();//字典类型
            LambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new LambdaQueryWrapper<SysDictData>();
            lambdaQueryWrapper.eq(SysDictData::getStatus, 0);//查询正常的字典数据
            List<SysDictData> list = this.list(lambdaQueryWrapper);
            for (SysDictData sysDictData : list) {
                if (!sysDictTypeMap.containsKey(sysDictData.getDictType())) {
                    sysDictTypeMap.put(sysDictData.getDictType(), sysDictData.getDictType());
                }
            }

            for (Map.Entry<String, String> entry : sysDictTypeMap.entrySet()) {
                Map<String, Object> sysDictDataMap = new HashMap<>();
                for (SysDictData sysDictData : list) {
                    if (entry.getKey().equals(sysDictData.getDictType())) {
                        if (!sysDictDataMap.containsKey(sysDictData.getDictLabel())) {
                            sysDictDataMap
                                .put(sysDictData.getDictLabel(), sysDictData.getDictValue());
                        }
                    }
                }
                sysDictCache.put(entry.getKey(),sysDictDataMap);
            }
            this.sysDictCache = sysDictCache;
            log.info("结束初始化字典数据缓存,缓存的数据sysDictCache={}", JSONUtil.toJsonStr(sysDictCache));
        }catch (Exception e){
          log.error("初始化字典数据缓存失败,具体失败原因:",e);
        }
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SysDictData> queryWrapper = new QueryWrapper<>();
        if(params.get("dictType")!=null &&!"".equals(params.get("dictType").toString())){
            queryWrapper.eq("dict_type",params.get("dictType").toString());
        }
        if(params.get("dictLabel")!=null &&!"".equals(params.get("dictLabel").toString())){
            queryWrapper.like("dict_label",params.get("dictLabel").toString());
        }
        if(params.get("dictValue")!=null &&!"".equals(params.get("dictValue").toString())){
            queryWrapper.eq("dict_value",params.get("dictValue").toString());
        }
        if(params.get("status")!=null &&!"".equals(params.get("status").toString())){
            queryWrapper.eq("status",params.get("status").toString());
        }
        IPage<SysDictData> page = this.page(
        new Query<SysDictData>().getPage(params),
        queryWrapper
        );
        return new PageUtils(page);
    }

    /**
     * 根据字典类型获取字典数据
     * @param dictType
     * @return
     */
    @Override
    public Map<String, Object> getSysDictByType(String dictType) {
        if(this.sysDictCache ==null || this.sysDictCache.size() == 0){
           return null;
        }
        return this.sysDictCache.get(dictType);
    }

    @Override
    public String getDictLabel(String dictType,String dictValue) {
        Map<String, Object>  sysDictMap = getSysDictByType(dictType);
        if(sysDictMap ==null || sysDictMap.size() == 0){
            return null;
        }
        for (Map.Entry<String,Object> entry:sysDictMap.entrySet()){
          if(entry.getValue().equals(dictValue)){
              return entry.getKey();
          }
        }
        return null;
    }

    @Override
    public String getDictLabelEn(String dictType, String dictValue) {
        LambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysDictData::getDictType,dictType);
        lambdaQueryWrapper.eq(SysDictData::getDictValue,dictValue);
        return this.getOne(lambdaQueryWrapper).getDictLabelEn();
    }

    @Override
    public String getDictValue(String dictType, String dictLable) {
        Map<String, Object>  sysDictMap = getSysDictByType(dictType);
        if(sysDictMap ==null || sysDictMap.size() == 0){
            return null;
        }
        for (Map.Entry<String,Object> entry:sysDictMap.entrySet()){
            if(entry.getKey().equals(dictLable)){
                return entry.getValue().toString();
            }
        }
        return null;
    }

    @Override
    public String getDictValueEn(String dictType, String dictLableEn) {
        LambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysDictData::getDictType,dictType);
        lambdaQueryWrapper.eq(SysDictData::getDictLabelEn,dictLableEn);
        return this.getOne(lambdaQueryWrapper).getDictValue();
    }

    @Override
    public List<SysDictData> getSysDictData(String tradeType) {
        LambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysDictData::getDictType,tradeType);
        return this.list(lambdaQueryWrapper);
    }


}
