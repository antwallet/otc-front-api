package cn.com.otc.modular.sys.bean.vo;

import lombok.Data;

/**
 * @Auther: 2024
 * @Date: 2024/10/25 11:18
 * @Description:申述前端bean
 */
@Data
public class TComplaintVo {


    /**
     * 付款订单ID
     */
    private String paymentId;

    /**
     * 问题描述
     */
    private String content;

    /**
     * 图片链接
     */
    private String imageUrl;

}
