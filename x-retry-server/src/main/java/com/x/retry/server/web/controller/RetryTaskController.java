package com.x.retry.server.web.controller;

import com.x.retry.server.model.dto.RetryTaskDTO;
import com.x.retry.server.service.RetryService;
import com.x.retry.server.service.RetryTaskService;
import com.x.retry.server.web.annotation.LoginRequired;
import com.x.retry.server.web.model.base.PageResult;
import com.x.retry.server.web.model.request.RetryTaskQueryVO;
import com.x.retry.server.web.model.response.RetryTaskResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2022-02-27
 * @since 2.0
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
}
