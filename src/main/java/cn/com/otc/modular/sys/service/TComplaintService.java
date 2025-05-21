package cn.com.otc.modular.sys.service;


import cn.com.otc.modular.auth.entity.result.UserInfoResult;
import cn.com.otc.modular.sys.bean.pojo.TComplaint;
import cn.com.otc.modular.sys.bean.vo.TComplaintVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Auther: 2024
 * @Date: 2024/10/25 13:35
 * @Description:
 */
public interface TComplaintService  extends IService<TComplaint> {

    //创建申请记录
    void createComplaintRecord(TComplaintVo tComplaintVo, String lang, UserInfoResult userInfoResult);

}
