package com.aizuda.snailjob.server.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 默认接口
 *
 * @author: opensnail
 * @date : 2022-03-28 22:17
 */
@Controller
public class WebController {

    @GetMapping
    public String forwardIndex() {
        return "index";
    }

}
