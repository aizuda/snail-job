package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.service.RetryTaskLogService;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskLogQueryVO;
import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskLogResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2022-02-27
 * @since 2.0
 */
@RestController
@RequestMapping("/retry-task-log")
public class RetryTaskLogController {

    @Autowired
    private RetryTaskLogService retryTaskLogService;

    @LoginRequired
    @GetMapping("list")
    public PageResult<List<RetryTaskLogResponseVO>> getRetryTaskLogPage(RetryTaskLogQueryVO queryVO) {
        return retryTaskLogService.getRetryTaskLogPage(queryVO);
    }

    @LoginRequired
    @GetMapping("{id}")
    public RetryTaskLogResponseVO getRetryTaskLogById(@PathVariable("id") Long id) {
        return retryTaskLogService.getRetryTaskLogById(id);
    }
}
