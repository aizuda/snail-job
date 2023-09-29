package com.aizuda.easy.retry.client.job.core.executor;

import com.aizuda.easy.retry.client.common.proxy.RequestBuilder;
import com.aizuda.easy.retry.client.job.core.client.JobNettyClient;
import com.aizuda.easy.retry.client.job.core.dto.JobContext;
import com.aizuda.easy.retry.client.model.ExecuteResult;
import com.aizuda.easy.retry.client.model.request.DispatchJobResultRequest;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.google.common.util.concurrent.FutureCallback;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-08 16:44
 * @since : 2.4.0
 */
@Slf4j
public class JobExecutorFutureCallback implements FutureCallback<ExecuteResult> {

    private static final JobNettyClient CLIENT = RequestBuilder.<JobNettyClient, NettyResult>newBuilder()
        .client(JobNettyClient.class)
        .callback(nettyResult -> LogUtils.info(log, "Data report successfully requestId:[{}]", nettyResult.getRequestId())).build();

    private JobContext jobContext;

    public JobExecutorFutureCallback(final JobContext jobContext) {
        this.jobContext = jobContext;
    }

    @Override
    public void onSuccess(final ExecuteResult result) {
        // 上报执行成功
        log.info("任务执行成功 [{}]", JsonUtil.toJsonString(result));
        DispatchJobResultRequest dispatchJobRequest = new DispatchJobResultRequest();
        if (result.getStatus() == StatusEnum.NO.getStatus()) {
            dispatchJobRequest.setTaskStatus(JobTaskStatusEnum.FAIL.getStatus());
        } else {
            dispatchJobRequest.setTaskStatus(JobTaskStatusEnum.SUCCESS.getStatus());
        }

        dispatchJobRequest.setTaskBatchId(jobContext.getTaskId());
        dispatchJobRequest.setGroupName(jobContext.getGroupName());
        dispatchJobRequest.setJobId(jobContext.getJobId());
        dispatchJobRequest.setTaskId(jobContext.getTaskId());
        dispatchJobRequest.setTaskType(jobContext.getTaskType());
        dispatchJobRequest.setExecuteResult(result);
        CLIENT.dispatchResult(dispatchJobRequest);
    }

    @Override
    public void onFailure(final Throwable t) {
        // 上报执行失败
        log.error("任务执行失败 jobTask:[{}]", jobContext.getTaskId(), t);
        DispatchJobResultRequest dispatchJobRequest = new DispatchJobResultRequest();
        dispatchJobRequest.setTaskBatchId(jobContext.getTaskId());
        dispatchJobRequest.setGroupName(jobContext.getGroupName());
        dispatchJobRequest.setJobId(jobContext.getJobId());
        dispatchJobRequest.setTaskId(jobContext.getTaskId());
        dispatchJobRequest.setTaskStatus(JobTaskStatusEnum.FAIL.getStatus());
        dispatchJobRequest.setTaskType(jobContext.getTaskType());
        dispatchJobRequest.setExecuteResult(ExecuteResult.failure(t.getMessage()));
        CLIENT.dispatchResult(dispatchJobRequest);
    }
}
