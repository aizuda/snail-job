package com.aizuda.snailjob.client.job.core.openapi;

import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.client.job.core.dto.RequestAddJobDTO;
import com.aizuda.snailjob.client.job.core.dto.RequestUpdateJobDTO;
import com.aizuda.snailjob.client.job.core.dto.RequestUpdateStatusDTO;
import com.aizuda.snailjob.common.core.model.Result;

public interface OpenApiClient {
    @Mapping(method = RequestMethod.POST, path = "/api/job/add")
    Result<Object> addJob(RequestAddJobDTO requestAddJobDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/update")
    Result<Object> updateJob(RequestUpdateJobDTO requestUpdateJobDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/getJobDetail")
    Result<Object> getJobDetail(Long jobId);

    @Mapping(method = RequestMethod.POST, path = "/api/job/triggerJob")
    Result<Object> triggerJob(Long triggerId);

    @Mapping(method = RequestMethod.POST, path = "/api/job/triggerWorkFlow")
    Result<Object> triggerWorkFlow(Long triggerId);

    @Mapping(method = RequestMethod.POST, path = "/api/job/updateJobStatus")
    Result<Object> updateJobStatus(RequestUpdateStatusDTO statusDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/updateWorkFlowStatus")
    Result<Object> updateWorkFlowStatus(RequestUpdateStatusDTO statusDTO);
}
