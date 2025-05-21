package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.exception.RRException;
import cn.com.otc.common.redis.NoRepeatSubmit;
import cn.com.otc.common.utils.CheckTokenUtil;
import cn.com.otc.common.utils.R;
import cn.com.otc.modular.sys.bean.pojo.TMessageIdRecord;
import cn.com.otc.modular.sys.service.TMessageIdRecordService;
import cn.com.otc.modular.sys.service.TUserService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * @description: 需要给机器人发送的信息
 * @author: zhangliyan
 * @time: 2024/2/26
 */
@Slf4j
@RestController
@RequestMapping("/api/front/messageIdRecord")
public class TMessageIdRecordController {

    @Autowired
    private TMessageIdRecordService messageIdRecordService;
    @Autowired
    private CheckTokenUtil checkTokenUtil;
    @Autowired
    private TUserService tUserService;


     /**
     * 功能描述: 判断是否拆开红包
     *
     * @auther: 2024
     * @date: 2024/7/11
     */
    @NoRepeatSubmit()
    @RequestMapping("/changeMessageIdRecord")
    public R messageIdRecord(HttpServletRequest httpRequest, @RequestParam("messageId") String messageId
        , @RequestParam("query") String query, @RequestParam("shareUserId") String shareUserId) {
        try {
            messageIdRecordService.messageIdRecord(messageId, query,shareUserId);
            return R.ok();
        } catch (Exception e) {
            log.error("更新是否展示弹窗失败,具体失败信息:", e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            return R.error();
        }
    }

    /**
     * 功能描述: 查询是否生成红包记录
     *
     * @auther: 2024
     * @date: 2024/7/11
     */
    @NoRepeatSubmit()
    @RequestMapping("/selectMessageIdRecord")
    public R selectMessageIdRecord(HttpServletRequest httpRequest, @RequestParam("query") String query) {
        try {
            List<TMessageIdRecord> tMessageIdRecord = messageIdRecordService.selectMessageIdRecord(query);
            HashMap<String, Object> map = new HashMap<>();
            map.put("messageIdRecord", tMessageIdRecord.get(0));
            return R.ok(map);
        } catch (Exception e) {
            log.error("更新是否展示弹窗失败,具体失败信息:", e);
            if (e instanceof RRException) {
                RRException rrException = (RRException) e;
                return R.error(rrException.getCode(), rrException.getMsg());
            }
            return R.error();
        }
    }


}
