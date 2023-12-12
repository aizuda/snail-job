package com.aizuda.easy.retry.server.web.service.impl;

import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO;
import com.aizuda.easy.retry.server.web.service.WorkflowService;
import org.springframework.stereotype.Service;

/**
 * @author xiaowoniu
 * @date 2023-12-12 21:54:05
 * @since 2.6.0
 */
@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Override
    public boolean saveWorkflow(WorkflowRequestVO workflowRequestVO) {
        return false;
    }
}
