package com.aizuda.snailjob.server.openapi.job.service.impl;

import com.aizuda.snailjob.server.openapi.job.service.WorkflowApiService;
import com.aizuda.snailjob.server.openapi.util.OpenApiSessionUtils;
import com.aizuda.snailjob.server.service.service.impl.AbstractWorkflowService;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
@Component("workflowApiService")
public class WorkflowApiServiceImpl extends AbstractWorkflowService implements WorkflowApiService {

    @Override
    protected String getNamespaceId() {
        return OpenApiSessionUtils.getNamespaceId();
    }
}
