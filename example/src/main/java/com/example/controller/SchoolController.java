package com.example.controller;

import com.x.retry.common.core.constant.SystemConstants;
import com.x.retry.common.core.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 学校 前端控制器
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2022-03-24
 */
@RestController
@RequestMapping("/school")
public class SchoolController {

    @GetMapping("/id")
    public Result getSchool(HttpServletRequest request) {
        String header = request.getHeader(SystemConstants.X_RETRY_HEAD);
        System.out.println(header);
        return new Result("school");
    }

}
