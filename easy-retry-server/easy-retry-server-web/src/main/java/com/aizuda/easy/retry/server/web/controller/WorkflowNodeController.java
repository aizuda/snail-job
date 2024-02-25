package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.web.service.WorkflowNodeService;
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
    public Boolean stop(@PathVariable("nodeId") Long nodeId,
                        @PathVariable("workflowTaskBatchId") Long workflowTaskBatchId) {
        return workflowNodeService.stop(nodeId, workflowTaskBatchId);
    }

    @PostMapping("/retry/{nodeId}/{workflowTaskBatchId}")
    public Boolean retry(@PathVariable("nodeId") Long nodeId,
                         @PathVariable("workflowTaskBatchId") Long workflowTaskBatchId) {
        return workflowNodeService.retry(nodeId, workflowTaskBatchId);
    }
}
