package com.aizuda.snailjob.server.web.service;

/**
 * @author xiaowoniu
 * @date 2024-02-03 21:24:52
 * @since 2.6.0
 */
public interface WorkflowNodeService {
    Boolean stop(Long nodeId, Long workflowTaskBatchId);

    Boolean retry(Long id, Long workflowTaskBatchId);
}
