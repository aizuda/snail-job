package com.aizuda.snailjob.server.openapi.job.api;

import com.aizuda.snailjob.server.common.dto.JobTriggerDTO;
import com.aizuda.snailjob.server.openapi.job.service.JobApiService;
import com.aizuda.snailjob.server.openapi.job.service.WorkflowApiService;
import com.aizuda.snailjob.server.service.dto.JobStatusUpdateRequestDTO;
import com.aizuda.snailjob.server.service.service.WorkflowService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.*;

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
public class WorkflowApi {
    private final WorkflowApiService apiService;
    private final WorkflowService workflowApiService;

    @DeleteMapping(OPENAPI_DELETE_WORKFLOW_V2)
    public boolean deleteWorkflowByIds(@RequestBody
                                       @NotEmpty(message = "ids cannot be null")
                                       @Size(max = 100, message = "Maximum {max} deletions") Set<Long> ids) {
        return workflowApiService.deleteWorkflowByIds(ids);
    }

    @PostMapping(OPENAPI_TRIGGER_WORKFLOW_V2)
    public boolean triggerWorkFlow(@RequestBody @Validated JobTriggerDTO jobTriggerDTO) {
        return workflowApiService.triggerWorkFlow(jobTriggerDTO);
    }

    @PostMapping(OPENAPI_UPDATE_WORKFLOW_STATUS_V2)
    public boolean updateWorkFlowStatus(@RequestBody @Validated JobStatusUpdateRequestDTO requestDTO) {
        return workflowApiService.updateWorkFlowStatus(requestDTO);
    }
}
