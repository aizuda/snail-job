package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import com.aizuda.snailjob.server.web.service.WorkflowNodeService;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.annotation.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaowoniu
 * @date 2023-12-23 17:47:51
 * @since 2.6.0
 */
@RestController
@RequestMapping("/workflow/node")
@RequiredArgsConstructor
public class WorkflowNodeController {
    private final WorkflowNodeService workflowNodeService;

    @PostMapping("/stop/{nodeId}/{workflowTaskBatchId}")
    @LoginRequired(role = RoleEnum.USER)
    public Boolean stop(@PathVariable("nodeId") Long nodeId,
                        @PathVariable("workflowTaskBatchId") Long workflowTaskBatchId) {
        return workflowNodeService.stop(nodeId, workflowTaskBatchId);
    }

    @PostMapping("/retry/{nodeId}/{workflowTaskBatchId}")
    @LoginRequired(role = RoleEnum.USER)
    public Boolean retry(@PathVariable("nodeId") Long nodeId,
                         @PathVariable("workflowTaskBatchId") Long workflowTaskBatchId) {
        return workflowNodeService.retry(nodeId, workflowTaskBatchId);
    }
}
