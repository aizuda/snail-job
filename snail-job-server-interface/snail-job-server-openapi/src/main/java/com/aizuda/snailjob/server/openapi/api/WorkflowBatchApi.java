package com.aizuda.snailjob.server.openapi.api;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.server.openapi.dto.WorkflowDetailResponseApiDTO;
import com.aizuda.snailjob.server.openapi.service.WorkflowBatchApiService;
import com.aizuda.snailjob.server.service.service.WorkflowBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
@RestController
@RequiredArgsConstructor
public class WorkflowBatchApi {
    private final WorkflowBatchApiService workflowBatchApiService;
    @Qualifier("workflowBatchApiCommonService")
    private final WorkflowBatchService workflowBatchService;

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_GET_WORKFLOW_BATCH_DETAIL_V2)
    public WorkflowDetailResponseApiDTO getWorkflowBatchById(@RequestParam("id") Long id) {
        return workflowBatchService.getWorkflowBatchById(id, WorkflowDetailResponseApiDTO.class);
    }
}
