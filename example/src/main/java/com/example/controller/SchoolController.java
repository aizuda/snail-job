package com.example.controller;

import com.x.retry.client.core.intercepter.RetrySiteSnapshot;
import com.x.retry.common.core.constant.SystemConstants;
import com.x.retry.common.core.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public Result getSchool(HttpServletRequest request, HttpServletResponse response) {

        String header = request.getHeader(SystemConstants.X_RETRY_HEAD_KEY);
        System.out.println(header);

        if (RetrySiteSnapshot.isRetryFlow()) {
            response.addHeader(SystemConstants.X_RETRY_STATUS_CODE_KEY, SystemConstants.X_RETRY_STATUS_CODE);
        }

        if (true) {
            throw new UnsupportedOperationException("异常测试");
        }

        return new Result(0, "school");
    }

}
