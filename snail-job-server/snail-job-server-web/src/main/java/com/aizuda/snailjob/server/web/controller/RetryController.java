package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.server.web.model.response.RetryResponseVO;
import com.aizuda.snailjob.server.web.service.RetryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 重试数据管理接口
 *
 * @author opensnail
 * @date 2022-02-27
 */
@RestController
@RequestMapping("/retry")
public class RetryController {

    @Autowired
    private RetryService retryService;

    @LoginRequired
    @GetMapping("list")
    public PageResult<List<RetryResponseVO>> getRetryTaskPage(RetryQueryVO queryVO) {
        return retryService.getRetryPage(queryVO);
    }

    @LoginRequired
    @GetMapping("{id}")
    public RetryResponseVO getRetryTaskById(@RequestParam("groupName") String groupName,
                                            @PathVariable("id") Long id) {
        return retryService.getRetryById(groupName, id);
    }

    @LoginRequired
    @PutMapping("status")
    public int updateRetryTaskStatus(@RequestBody RetryUpdateStatusRequestVO retryUpdateStatusRequestVO) {
        return retryService.updateRetryStatus(retryUpdateStatusRequestVO);
    }

    @LoginRequired
    @PostMapping
    public int saveRetryTask(@RequestBody @Validated RetrySaveRequestVO retryTaskRequestVO) {
        return retryService.saveRetryTask(retryTaskRequestVO);
    }

    @LoginRequired
    @PostMapping("/generate/idempotent-id")
    public Result<String> idempotentIdGenerate(@RequestBody @Validated GenerateRetryIdempotentIdVO generateRetryIdempotentIdVO) {
        return new Result<>(retryService.idempotentIdGenerate(generateRetryIdempotentIdVO));
    }


    @LoginRequired
    @PutMapping("/batch")
    public Integer updateRetryTaskExecutorName(@RequestBody @Validated RetryUpdateExecutorNameRequestVO requestVO) {
        return retryService.updateRetryExecutorName(requestVO);
    }

    @LoginRequired
    @DeleteMapping("/batch")
    public boolean batchDeleteRetry(@RequestBody @Validated BatchDeleteRetryTaskVO requestVO) {
        return retryService.batchDeleteRetry(requestVO);
    }

    @LoginRequired
    @PostMapping("/batch")
    public Integer parseLogs(@RequestBody @Validated ParseLogsVO parseLogsVO) {
        return retryService.parseLogs(parseLogsVO);
    }

    @LoginRequired
    @PostMapping("/manual/trigger/retry/task")
    public boolean manualTriggerRetryTask(@RequestBody @Validated ManualTriggerTaskRequestVO requestVO) {
        return retryService.manualTriggerRetryTask(requestVO);
    }

}
