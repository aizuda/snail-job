package com.aizuda.snailjob.client.job.core.openapi;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.annotation.Param;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.model.request.*;
import com.aizuda.snailjob.model.request.base.JobRequest;
import com.aizuda.snailjob.model.request.base.StatusUpdateRequest;
import com.aizuda.snailjob.model.request.base.WorkflowTriggerRequest;
import com.aizuda.snailjob.model.response.JobApiResponse;
import com.aizuda.snailjob.model.response.JobBatchApiResponse;
import com.aizuda.snailjob.model.response.JobExistsResponse;
import com.aizuda.snailjob.model.response.WorkflowDetailApiResponse;

import java.util.Set;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.*;

public interface JobOpenApiClientV2 {

    @Mapping(method = RequestMethod.POST, path = OPENAPI_ADD_JOB)
    Result<Long> addJob(JobRequest jobRequest);

    @Mapping(method = RequestMethod.PUT, path = OPENAPI_UPDATE_JOB)
    Result<Boolean> updateJob(JobRequest jobRequest);

    @Mapping(method = RequestMethod.GET, path = OPENAPI_GET_JOB_DETAIL_V2)
    Result<JobApiResponse> getJobDetail(@Param("id") Long jobId);

    @Mapping(method = RequestMethod.GET, path = OPENAPI_GET_JOB_BATCH_DETAIL_V2)
    Result<JobBatchApiResponse> getJobBatchDetail(@Param("id") Long jobBatchId);

    @Mapping(method = RequestMethod.GET, path = OPENAPI_GET_WORKFLOW_BATCH_DETAIL_V2)
    Result<WorkflowDetailApiResponse> getWorkflowBatchDetail(@Param("id") Long jobBatchId);

    @Mapping(method = RequestMethod.POST, path = OPENAPI_TRIGGER_JOB_V2)
    Result<Boolean> triggerJob(JobTriggerApiRequest request);

    @Mapping(method = RequestMethod.POST, path = OPENAPI_TRIGGER_WORKFLOW_V2)
    Result<Boolean> triggerWorkFlow(WorkflowTriggerRequest jobTriggerDTO);

    @Mapping(method = RequestMethod.PUT, path = OPENAPI_UPDATE_JOB_STATUS_V2)
    Result<Boolean> updateJobStatus(StatusUpdateRequest statusDTO);

    @Mapping(method = RequestMethod.PUT, path = OPENAPI_UPDATE_WORKFLOW_STATUS_V2)
    Result<Boolean> updateWorkFlowStatus(StatusUpdateRequest statusDTO);

    @Mapping(method = RequestMethod.DELETE, path = OPENAPI_DELETE_JOB_V2)
    Result<Boolean> deleteJob(Set<Long> toDeleteIds);

    @Mapping(method = RequestMethod.DELETE, path = OPENAPI_DELETE_WORKFLOW_V2)
    Result<Boolean> deleteWorkflow(Set<Long> toDeleteIds);

    @Mapping(method = RequestMethod.GET, path = OPENAPI_EXISTS_JOB_BY_ID)
    Result<JobExistsResponse> existsJobById(@Param("id") Long id);

    @Mapping(method = RequestMethod.GET, path = OPENAPI_EXISTS_WORKFLOW_BY_ID)
    Result<JobExistsResponse> existsWorkflowById(@Param("id") Long id);

    // ==================== job bizId 接口 ====================

    @Mapping(method = RequestMethod.POST, path = OPENAPI_ADD_JOB_BY_BIZ_ID)
    Result<Long> addJobByBizId(JobBizIdApiRequest jobRequest);

    @Mapping(method = RequestMethod.PUT, path = OPENAPI_UPDATE_JOB_BY_BIZ_ID)
    Result<Boolean> updateJobByBizId(JobBizIdApiRequest jobRequest);

    @Mapping(method = RequestMethod.GET, path = OPENAPI_GET_JOB_DETAIL_BY_BIZ_ID)
    Result<JobApiResponse> getJobDetailByBizId(@Param("bizId") String bizId);

    @Mapping(method = RequestMethod.POST, path = OPENAPI_TRIGGER_JOB_BY_BIZ_ID)
    Result<Boolean> triggerJobByBizId(JobTriggerBizIdRequest request);

    @Mapping(method = RequestMethod.DELETE, path = OPENAPI_DELETE_JOB_BY_BIZ_IDS)
    Result<Boolean> deleteJobByBizIds(DeleteBizIdRequest request);

    @Mapping(method = RequestMethod.PUT, path = OPENAPI_UPDATE_JOB_STATUS_BY_BIZ_ID)
    Result<Boolean> updateJobStatusByBizId(StatusUpdateBizIdRequest request);

    @Mapping(method = RequestMethod.GET, path = OPENAPI_EXISTS_JOB_BY_BIZ_ID)
    Result<JobExistsResponse> existsJobByBizId(@Param("bizId") String bizId);

    // ==================== workflow bizId 接口 ====================

    @Mapping(method = RequestMethod.POST, path = OPENAPI_TRIGGER_WORKFLOW_BY_BIZ_ID)
    Result<Boolean> triggerWorkflowByBizId(WorkflowTriggerBizIdRequest request);

    @Mapping(method = RequestMethod.DELETE, path = OPENAPI_DELETE_WORKFLOW_BY_BIZ_IDS)
    Result<Boolean> deleteWorkflowByBizIds(@Param("bizIds") Set<String> bizIds);

    @Mapping(method = RequestMethod.PUT, path = OPENAPI_UPDATE_WORKFLOW_STATUS_BY_BIZ_ID)
    Result<Boolean> updateWorkflowStatusByBizId(WorkflowStatusUpdateBizIdRequest request);

    @Mapping(method = RequestMethod.GET, path = OPENAPI_EXISTS_WORKFLOW_BY_BIZ_ID)
    Result<JobExistsResponse> existsWorkflowByBizId(@Param("bizId") String bizId);
}
