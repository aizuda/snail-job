package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.RetryTaskLogMessageQueryVO;
import com.aizuda.snailjob.server.web.model.request.RetryTaskQueryVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskLogMessageResponseVO;
import com.aizuda.snailjob.server.web.model.response.RetryTaskResponseVO;
import com.aizuda.snailjob.server.web.service.RetryTaskService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 重试日志接口
 *
 * @author opensnail
 * @date 2022-02-27
 */
@RestController
@RequestMapping("/retry-task")
public class RetryTaskController {

    @Autowired
    private RetryTaskService retryTaskService;

    @LoginRequired
    @GetMapping("list")
    public PageResult<List<RetryTaskResponseVO>> getRetryTaskPage(RetryTaskQueryVO queryVO) {
        return retryTaskService.getRetryTaskLogPage(queryVO);
    }

    @LoginRequired
    @GetMapping("/message/list")
    public RetryTaskLogMessageResponseVO getRetryTaskLogMessagePage(RetryTaskLogMessageQueryVO queryVO) {
        return retryTaskService.getRetryTaskLogMessagePage(queryVO);
    }

    @LoginRequired
    @GetMapping("{id}")
    public RetryTaskResponseVO getRetryTaskById(@PathVariable("id") Long id) {
        return retryTaskService.getRetryTaskById(id);
    }

    @LoginRequired
    @PostMapping("/stop/{id}")
    public Boolean stopById(@PathVariable("id") Long id) {
        return retryTaskService.stopById(id);
    }

    @LoginRequired
    @DeleteMapping("{id}")
    public Boolean deleteById(@PathVariable("id") Long id) {
        return retryTaskService.deleteById(id);
    }

    @LoginRequired
    @DeleteMapping("ids")
    public Boolean batchDelete(@RequestBody @NotEmpty(message = "ids不能为空") Set<Long> ids) {
        return retryTaskService.batchDelete(ids);
    }


}
