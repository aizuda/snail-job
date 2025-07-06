package com.aizuda.snailjob.server.openapi.job.service.impl;

import com.aizuda.snailjob.server.openapi.job.service.WorkflowBatchApiService;
import com.aizuda.snailjob.server.service.service.impl.AbstractWorkflowBatchService;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
@Component("workflowBatchApiService")
public class WorkflowBatchApiServiceImpl extends AbstractWorkflowBatchService implements WorkflowBatchApiService {
    private static final Integer WORKFLOW_DECISION_FAILED_STATUS = 98;



}
