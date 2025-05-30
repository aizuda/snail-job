package com.aizuda.snailjob.client.job.core.openapi;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.client.job.core.dto.JobTriggerDTO;
import com.aizuda.snailjob.client.job.core.dto.RequestAddOrUpdateJobDTO;
import com.aizuda.snailjob.client.job.core.dto.RequestUpdateStatusDTO;
import com.aizuda.snailjob.common.core.model.Result;

import java.util.Set;

public interface JobOpenApiClient {
    @Mapping(method = RequestMethod.POST, path = "/api/job/add")
    Result<Object> addJob(RequestAddOrUpdateJobDTO requestAddOrUpdateJobDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/update")
    Result<Object> updateJob(RequestAddOrUpdateJobDTO requestUpdateJobDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/getJobDetail")
    Result<Object> getJobDetail(Long jobId);

    @Mapping(method = RequestMethod.POST, path = "/api/job/getJobBatchDetail")
    Result<Object> getJobBatchDetail(Long jobBatchId);

    @Mapping(method = RequestMethod.POST, path = "/api/job/getWorkflowBatchDetail")
    Result<Object> getWorkflowBatchDetail(Long jobBatchId);

    @Mapping(method = RequestMethod.POST, path = "/api/job/triggerJob")
    Result<Object> triggerJob(JobTriggerDTO jobTriggerDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/triggerWorkFlow")
    Result<Object> triggerWorkFlow(JobTriggerDTO jobTriggerDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/updateJobStatus")
    Result<Object> updateJobStatus(RequestUpdateStatusDTO statusDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/updateWorkFlowStatus")
    Result<Object> updateWorkFlowStatus(RequestUpdateStatusDTO statusDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/deleteJob")
    Result<Object> deleteJob(Set<Long> toDeleteIds);

    @Mapping(method = RequestMethod.POST, path = "/api/job/deleteWorkFlow")
    Result<Object> deleteWorkflow(Set<Long> toDeleteIds);
}
