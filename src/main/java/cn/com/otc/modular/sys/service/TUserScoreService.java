package cn.com.otc.modular.sys.service;

import cn.com.otc.common.utils.PageUtils;
import cn.com.otc.modular.sys.bean.pojo.TUserScore;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户评分依据表 服务类
 * </p>
 *
 * @author zhangliyan
 * @since 2024-02-01
 */
public interface TUserScoreService extends IService<TUserScore> {
        PageUtils queryPage(Map<String, Object> params);

        void savetUserScore(String scoreType, String scoreValue, String userTGID);

        List<TUserScore> getTUserScore(String userTGID, ArrayList<String> strings);

}
