package com.aizuda.snailjob.client.job.core.openapi;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.annotation.RequestParam;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.client.job.core.dto.JobTriggerDTO;
import com.aizuda.snailjob.client.job.core.dto.RequestAddOrUpdateJobDTO;
import com.aizuda.snailjob.client.job.core.dto.RequestUpdateStatusDTO;
import com.aizuda.snailjob.common.core.model.Result;

import java.util.Set;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.*;

public interface JobOpenApiClientV2 {

    @Mapping(method = RequestMethod.POST, path = OPENAPI_ADD_JOB)
    Result<Object> addJob(RequestAddOrUpdateJobDTO requestAddOrUpdateJobDTO);

    @Mapping(method = RequestMethod.POST, path = OPENAPI_UPDATE_JOB)
    Result<Object> updateJob(RequestAddOrUpdateJobDTO requestUpdateJobDTO);

    @Mapping(method = RequestMethod.GET, path = OPENAPI_GET_JOB_DETAIL_V2)
    Result<Object> getJobDetail(@RequestParam("id") Long jobId);

    @Mapping(method = RequestMethod.GET, path = OPENAPI_GET_JOB_BATCH_DETAIL_V2)
    Result<Object> getJobBatchDetail(@RequestParam("id") Long jobBatchId);

    @Mapping(method = RequestMethod.GET, path = OPENAPI_GET_WORKFLOW_BATCH_DETAIL_V2)
    Result<Object> getWorkflowBatchDetail(@RequestParam("id") Long jobBatchId);

    @Mapping(method = RequestMethod.POST, path = OPENAPI_TRIGGER_JOB_V2)
    Result<Object> triggerJob(JobTriggerDTO jobTriggerDTO);

    @Mapping(method = RequestMethod.POST, path = OPENAPI_TRIGGER_WORKFLOW_V2)
    Result<Object> triggerWorkFlow(JobTriggerDTO jobTriggerDTO);

    @Mapping(method = RequestMethod.POST, path = OPENAPI_UPDATE_JOB_STATUS_V2)
    Result<Object> updateJobStatus(RequestUpdateStatusDTO statusDTO);

    @Mapping(method = RequestMethod.PUT, path = OPENAPI_UPDATE_WORKFLOW_STATUS_V2)
    Result<Object> updateWorkFlowStatus(RequestUpdateStatusDTO statusDTO);

    @Mapping(method = RequestMethod.DELETE, path = OPENAPI_DELETE_JOB_V2)
    Result<Object> deleteJob(Set<Long> toDeleteIds);

    @Mapping(method = RequestMethod.DELETE, path = OPENAPI_DELETE_WORKFLOW_V2)
    Result<Object> deleteWorkflow(Set<Long> toDeleteIds);
}
