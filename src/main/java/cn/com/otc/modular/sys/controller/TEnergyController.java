package cn.com.otc.modular.sys.controller;

import cn.com.otc.common.utils.CheckTokenUtil;
import cn.com.otc.modular.sys.service.TEnergyService;
import cn.com.otc.modular.sys.service.TUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: zhangliyan
 * @time: 2024/3/4
 */
@Slf4j
@RestController
@RequestMapping("/api/front/tenergy")
public class TEnergyController {

    @Autowired
    private TEnergyService tEnergyService;
    @Autowired
    private TUserService tUserService;
    @Autowired
    private CheckTokenUtil checkTokenUtil;



}
