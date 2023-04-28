package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.service.RetryTaskService;
import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.GenerateRetryBizIdVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskQueryVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskUpdateStatusRequestVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskSaveRequestVO;
import com.aizuda.easy.retry.server.web.model.request.RetryTaskUpdateExecutorNameRequestVO;
import com.aizuda.easy.retry.server.web.model.response.RetryTaskResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
    @PostMapping("/generate/biz-id")
    public String bizIdGenerate(@RequestBody @Validated GenerateRetryBizIdVO generateRetryBizIdVO) {
        return retryTaskService.bizIdGenerate(generateRetryBizIdVO);
    }

    @LoginRequired
    @PutMapping("/executor-name/batch")
    public Integer updateRetryTaskExecutorName(@RequestBody @Validated RetryTaskUpdateExecutorNameRequestVO requestVO) {
        return retryTaskService.updateRetryTaskExecutorName(requestVO);
    }
}
