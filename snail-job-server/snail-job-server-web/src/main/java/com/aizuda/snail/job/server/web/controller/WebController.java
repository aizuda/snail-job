package com.aizuda.snail.job.server.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 默认接口
 *
 * @author: opensnail
 * @date : 2022-03-28 22:17
 */
@Controller
public class WebController {

    @RequestMapping(value = {
            "/admin"
    })
    public String fowardIndex() {
        return "index";
    }

}
