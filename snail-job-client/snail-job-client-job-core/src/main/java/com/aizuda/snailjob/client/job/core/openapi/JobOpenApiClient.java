package com.aizuda.snailjob.client.job.core.openapi;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.model.base.StatusUpdateRequest;
import com.aizuda.snailjob.model.request.JobApiRequest;
import com.aizuda.snailjob.model.request.JobTriggerApiRequest;
import com.aizuda.snailjob.model.request.WorkflowTriggerApiRequest;

import java.util.Set;

@Deprecated(since = "1.7.0")
public interface JobOpenApiClient {
    @Mapping(method = RequestMethod.POST, path = "/api/job/add")
    Result<Object> addJob(JobApiRequest jobApiRequest);

    @Mapping(method = RequestMethod.POST, path = "/api/job/update")
    Result<Object> updateJob(JobApiRequest jobApiRequest);

    @Mapping(method = RequestMethod.POST, path = "/api/job/getJobDetail")
    Result<Object> getJobDetail(Long jobId);

    @Mapping(method = RequestMethod.POST, path = "/api/job/getJobBatchDetail")
    Result<Object> getJobBatchDetail(Long jobBatchId);

    @Mapping(method = RequestMethod.POST, path = "/api/job/getWorkflowBatchDetail")
    Result<Object> getWorkflowBatchDetail(Long jobBatchId);

    @Mapping(method = RequestMethod.POST, path = "/api/job/triggerJob")
    Result<Object> triggerJob(JobTriggerApiRequest request);

    @Mapping(method = RequestMethod.POST, path = "/api/job/triggerWorkFlow")
    Result<Object> triggerWorkFlow(WorkflowTriggerApiRequest jobTriggerDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/updateJobStatus")
    Result<Object> updateJobStatus(StatusUpdateRequest statusDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/updateWorkFlowStatus")
    Result<Object> updateWorkFlowStatus(StatusUpdateRequest statusDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/deleteJob")
    Result<Object> deleteJob(Set<Long> toDeleteIds);

    @Mapping(method = RequestMethod.POST, path = "/api/job/deleteWorkFlow")
    Result<Object> deleteWorkflow(Set<Long> toDeleteIds);
}
