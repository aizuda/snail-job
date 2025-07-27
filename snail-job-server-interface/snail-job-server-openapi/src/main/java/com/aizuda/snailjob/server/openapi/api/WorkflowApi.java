package com.aizuda.snailjob.server.openapi.api;

import com.aizuda.snailjob.model.request.StatusUpdateApiRequest;
import com.aizuda.snailjob.model.request.JobTriggerApiRequest;
import com.aizuda.snailjob.model.request.WorkflowTriggerApiRequest;
import com.aizuda.snailjob.server.openapi.service.WorkflowApiService;
import com.aizuda.snailjob.server.service.service.WorkflowService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @Qualifier("workflowApiCommonService")
    private final WorkflowService workflowService;

    @DeleteMapping(OPENAPI_DELETE_WORKFLOW_V2)
    public boolean deleteWorkflowByIds(@RequestBody
                                       @NotEmpty(message = "ids cannot be null")
                                       @Size(max = 100, message = "Maximum {max} deletions") Set<Long> ids) {
        return workflowService.deleteWorkflowByIds(ids);
    }

    @PostMapping(OPENAPI_TRIGGER_WORKFLOW_V2)
    public boolean triggerWorkFlow(@RequestBody @Validated WorkflowTriggerApiRequest jobTriggerApiDTO) {
        return workflowService.triggerWorkFlow(jobTriggerApiDTO);
    }

    @PutMapping(OPENAPI_UPDATE_WORKFLOW_STATUS_V2)
    public boolean updateWorkFlowStatus(@RequestBody @Validated StatusUpdateApiRequest requestDTO) {
        return workflowService.updateWorkFlowStatus(requestDTO);
    }
}
