package com.x.retry.server.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: www.byteblogs.com
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
