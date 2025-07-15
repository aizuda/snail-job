package com.aizuda.snailjob.server.openapi.job.api;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.server.common.vo.WorkflowDetailResponseVO;
import com.aizuda.snailjob.server.openapi.job.dto.WorkflowDetailResponseDTO;
import com.aizuda.snailjob.server.openapi.job.service.WorkflowBatchApiService;
import com.aizuda.snailjob.server.service.service.WorkflowBatchService;
import lombok.RequiredArgsConstructor;
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
    private final WorkflowBatchApiService apiService;
    private final WorkflowBatchService workflowBatchApiService;

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_GET_WORKFLOW_BATCH_DETAIL_V2)
    public WorkflowDetailResponseDTO getWorkflowBatchById(@RequestParam("id") Long id) {
        return (WorkflowDetailResponseDTO) workflowBatchApiService.getWorkflowBatchById(id);
    }
}
