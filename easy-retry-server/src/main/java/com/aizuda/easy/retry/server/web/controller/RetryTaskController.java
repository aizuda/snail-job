package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.service.RetryTaskService;
import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.*;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 重试数据管理接口
 *
 * @author www.byteblogs.com
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
        return retryTaskService.getRetryTaskPage(queryVO);
    }

    @LoginRequired
    @GetMapping("{id}")
    public RetryTaskResponseVO getRetryTaskById(@RequestParam("groupName") String groupName,
        @PathVariable("id") Long id) {
        return retryTaskService.getRetryTaskById(groupName, id);
    }

    @LoginRequired
    @PutMapping("status")
    public int updateRetryTaskStatus(@RequestBody RetryTaskUpdateStatusRequestVO retryTaskUpdateStatusRequestVO) {
        return retryTaskService.updateRetryTaskStatus(retryTaskUpdateStatusRequestVO);
    }

    @LoginRequired
    @PostMapping
    public int saveRetryTask(@RequestBody @Validated RetryTaskSaveRequestVO retryTaskRequestVO) {
        return retryTaskService.saveRetryTask(retryTaskRequestVO);
    }

    @LoginRequired
    @PostMapping("/generate/idempotent-id")
    public Result<String> idempotentIdGenerate(@RequestBody @Validated GenerateRetryIdempotentIdVO generateRetryIdempotentIdVO) {
        return new Result<>(retryTaskService.idempotentIdGenerate(generateRetryIdempotentIdVO));
    }

    @LoginRequired
    @PutMapping("/batch")
    public Integer updateRetryTaskExecutorName(@RequestBody @Validated RetryTaskUpdateExecutorNameRequestVO requestVO) {
        return retryTaskService.updateRetryTaskExecutorName(requestVO);
    }

    @LoginRequired
    @DeleteMapping("/batch")
    public Integer deleteRetryTask(@RequestBody @Validated BatchDeleteRetryTaskVO requestVO) {
        return retryTaskService.deleteRetryTask(requestVO);
    }

    @LoginRequired
    @PostMapping("/batch")
    public Integer parseLogs(@RequestBody @Validated ParseLogsVO parseLogsVO) {
        return retryTaskService.parseLogs(parseLogsVO);
    }
}
