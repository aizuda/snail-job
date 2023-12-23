package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.WorkflowBatchQueryVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowBatchResponseVO;
import com.aizuda.easy.retry.server.web.model.response.WorkflowDetailResponseVO;
import com.aizuda.easy.retry.server.web.service.WorkflowBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author xiaowoniu
 * @date 2023-12-23 17:47:51
 * @since 2.6.0
 */
@RestController
@RequestMapping("/workflow/batch")
@RequiredArgsConstructor
public class WorkflowBatchController {
    private final WorkflowBatchService workflowBatchService;

    @LoginRequired
    @GetMapping("/page/list")
    public PageResult<List<WorkflowBatchResponseVO>> listPage(WorkflowBatchQueryVO queryVO) {
        return workflowBatchService.listPage(queryVO);
    }

    @LoginRequired
    @GetMapping("{id}")
    public WorkflowDetailResponseVO getWorkflowBatchDetail(@PathVariable("id") Long id) {
        return workflowBatchService.getWorkflowBatchDetail(id);
    }
}
