package com.aizuda.easy.retry.server.web.service;

import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO;

/**
 * @author xiaowoniu
 * @date 2023-12-12 21:53:59
 * @since 2.6.0
 */
public interface WorkflowService {

    boolean saveWorkflow(WorkflowRequestVO workflowRequestVO);
}
