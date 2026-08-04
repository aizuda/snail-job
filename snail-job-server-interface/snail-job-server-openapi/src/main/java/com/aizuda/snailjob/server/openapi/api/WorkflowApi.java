package com.aizuda.snailjob.server.openapi.api;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.model.request.StatusUpdateApiRequest;
import com.aizuda.snailjob.model.request.WorkflowStatusUpdateBizIdRequest;
import com.aizuda.snailjob.model.request.WorkflowTriggerApiRequest;
import com.aizuda.snailjob.model.request.WorkflowTriggerBizIdRequest;
import com.aizuda.snailjob.model.response.JobExistsResponse;
import com.aizuda.snailjob.model.response.WorkflowExistsResponse;
import com.aizuda.snailjob.server.service.service.WorkflowService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

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
    @Qualifier("workflowApiCommonService")
    private final WorkflowService workflowService;

    @DeleteMapping(SystemConstants.HTTP_PATH.OPENAPI_DELETE_WORKFLOW_V2)
    public boolean deleteWorkflowByIds(@RequestBody
                                       @NotEmpty(message = "ids cannot be null")
                                       @Size(max = 100, message = "Maximum {max} deletions") Set<Long> ids) {
        return workflowService.deleteWorkflowByIds(ids);
    }

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_TRIGGER_WORKFLOW_V2)
    public boolean triggerWorkFlow(@RequestBody @Validated WorkflowTriggerApiRequest jobTriggerApiDTO) {
        return workflowService.triggerWorkFlow(jobTriggerApiDTO);
    }

    @PutMapping(SystemConstants.HTTP_PATH.OPENAPI_UPDATE_WORKFLOW_STATUS_V2)
    public boolean updateWorkFlowStatus(@RequestBody @Validated StatusUpdateApiRequest requestDTO) {
        return workflowService.updateWorkFlowStatus(requestDTO);
    }

    @GetMapping(SystemConstants.HTTP_PATH.OPENAPI_EXISTS_WORKFLOW_BY_ID)
    public WorkflowExistsResponse existsWorkflowById(@RequestParam("id") Long id) {
        return workflowService.existsWorkflowById(id);
    }

    // ==================== workflow bizId 接口 ====================

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_TRIGGER_WORKFLOW_BY_BIZ_ID)
    public boolean triggerWorkflowByBizId(@RequestBody @Validated WorkflowTriggerBizIdRequest request) {
        return workflowService.triggerWorkflowByBizId(request);
    }

    @DeleteMapping(SystemConstants.HTTP_PATH.OPENAPI_DELETE_WORKFLOW_BY_BIZ_IDS)
    public boolean deleteWorkflowByBizIds(@RequestBody
                                          @NotEmpty(message = "bizIds cannot be null")
                                          @Size(max = 100, message = "Maximum {max} deletions") Set<String> bizIds) {
        return workflowService.deleteWorkflowByBizIds(bizIds);
    }

    @PutMapping(SystemConstants.HTTP_PATH.OPENAPI_UPDATE_WORKFLOW_STATUS_BY_BIZ_ID)
    public boolean updateWorkflowStatusByBizId(@RequestBody @Validated WorkflowStatusUpdateBizIdRequest request) {
        return workflowService.updateWorkflowStatusByBizId(request);
    }


    @GetMapping(SystemConstants.HTTP_PATH.OPENAPI_EXISTS_WORKFLOW_BY_BIZ_ID)
    public WorkflowExistsResponse existsWorkflowByBizId(@RequestParam("bizId") String bizId) {
        return workflowService.existsWorkflowByBizId(bizId);
    }
}
