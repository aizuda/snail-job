package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.RetryTaskLogMessageQueryVO;
import com.aizuda.snailjob.server.web.model.request.RetryTaskLogQueryVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskLogMessageResponseVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskLogResponseVO;
import com.aizuda.snailjob.server.web.service.RetryTaskLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 重试日志接口
 *
 * @author opensnail
 * @date 2022-02-27
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
    @GetMapping("/message/list")
    public RetryTaskLogMessageResponseVO getRetryTaskLogPage(RetryTaskLogMessageQueryVO queryVO) {
        return retryTaskLogService.getRetryTaskLogMessagePage(queryVO);
    }

    @LoginRequired
    @GetMapping("{id}")
    public RetryTaskLogResponseVO getRetryTaskLogById(@PathVariable("id") Long id) {
        return retryTaskLogService.getRetryTaskLogById(id);
    }
}
