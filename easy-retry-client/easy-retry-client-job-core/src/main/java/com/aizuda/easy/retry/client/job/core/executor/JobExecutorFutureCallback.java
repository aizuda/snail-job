package com.aizuda.easy.retry.client.job.core.executor;

import com.aizuda.easy.retry.client.common.proxy.RequestBuilder;
import com.aizuda.easy.retry.client.common.util.ThreadLocalLogUtil;
import com.aizuda.easy.retry.client.job.core.cache.ThreadPoolCache;
import com.aizuda.easy.retry.client.job.core.client.JobNettyClient;
import com.aizuda.easy.retry.client.job.core.log.JobLogMeta;
import com.aizuda.easy.retry.client.model.ExecuteResult;
import com.aizuda.easy.retry.client.model.request.DispatchJobResultRequest;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskTypeEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.model.JobContext;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.enums.LogTypeEnum;
import com.google.common.util.concurrent.FutureCallback;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.CancellationException;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-08 16:44
 * @since : 2.4.0
 */
@Slf4j
public class JobExecutorFutureCallback implements FutureCallback<ExecuteResult> {

    private static final JobNettyClient CLIENT = RequestBuilder.<JobNettyClient, NettyResult>newBuilder()
            .client(JobNettyClient.class)
            .callback(nettyResult -> EasyRetryLog.LOCAL.info("Job execute result report successfully requestId:[{}]", nettyResult.getRequestId())).build();

    private final JobContext jobContext;

    public JobExecutorFutureCallback(final JobContext jobContext) {
        this.jobContext = jobContext;
    }

    @Override
    public void onSuccess(ExecuteResult result) {

        try {
            // 初始化调度信息（日志上报LogUtil）
            initLogContext();

            // 上报执行成功
            EasyRetryLog.REMOTE.info("任务执行成功 taskBatchId:[{}] [{}]", jobContext.getTaskBatchId(),
                JsonUtil.toJsonString(result));

            if (Objects.isNull(result)) {
                result = ExecuteResult.success();
            }

            int taskStatus;
            if (result.getStatus() == StatusEnum.NO.getStatus()) {
                taskStatus = JobTaskStatusEnum.FAIL.getStatus();
            } else {
                taskStatus = JobTaskStatusEnum.SUCCESS.getStatus();
            }

            CLIENT.dispatchResult(buildDispatchJobResultRequest(result, taskStatus));
        } catch (Exception e) {
            EasyRetryLog.REMOTE.error("执行结果上报异常.[{}]", jobContext.getTaskId(), e);
        } finally {
            ThreadLocalLogUtil.removeContext();
            stopThreadPool();
        }
    }

    @Override
    public void onFailure(final Throwable t) {

        try {
            // 初始化调度信息（日志上报LogUtil）
            initLogContext();

            // 上报执行失败
            EasyRetryLog.REMOTE.error("任务执行失败 taskBatchId:[{}]", jobContext.getTaskBatchId(), t);

            ExecuteResult failure = ExecuteResult.failure();
            if (t instanceof CancellationException) {
                failure.setMessage("任务被取消");
            } else {
                failure.setMessage(t.getMessage());
            }

            CLIENT.dispatchResult(
                buildDispatchJobResultRequest(failure, JobTaskStatusEnum.FAIL.getStatus())
            );
        } catch (Exception e) {
            EasyRetryLog.REMOTE.error("执行结果上报异常.[{}]", jobContext.getTaskId(), e);
        } finally {
            ThreadLocalLogUtil.removeContext();
            stopThreadPool();
        }
    }

    private void initLogContext() {
        JobLogMeta logMeta = new JobLogMeta();
        logMeta.setNamespaceId(jobContext.getNamespaceId());
        logMeta.setTaskId(jobContext.getTaskId());
        logMeta.setGroupName(jobContext.getGroupName());
        logMeta.setJobId(jobContext.getJobId());
        logMeta.setTaskBatchId(jobContext.getTaskBatchId());
        ThreadLocalLogUtil.initLogInfo(logMeta, LogTypeEnum.JOB);
    }

    private void stopThreadPool() {
        if (jobContext.getTaskType() == JobTaskTypeEnum.CLUSTER.getType()) {
            ThreadPoolCache.stopThreadPool(jobContext.getTaskBatchId());
        }
    }

    private DispatchJobResultRequest buildDispatchJobResultRequest(ExecuteResult executeResult, int status) {
        DispatchJobResultRequest dispatchJobRequest = new DispatchJobResultRequest();
        dispatchJobRequest.setTaskBatchId(jobContext.getTaskBatchId());
        dispatchJobRequest.setGroupName(jobContext.getGroupName());
        dispatchJobRequest.setJobId(jobContext.getJobId());
        dispatchJobRequest.setTaskId(jobContext.getTaskId());
        dispatchJobRequest.setWorkflowTaskBatchId(jobContext.getWorkflowTaskBatchId());
        dispatchJobRequest.setWorkflowNodeId(jobContext.getWorkflowNodeId());
        dispatchJobRequest.setTaskBatchId(jobContext.getTaskBatchId());
        dispatchJobRequest.setTaskId(jobContext.getTaskId());
        dispatchJobRequest.setTaskType(jobContext.getTaskType());
        dispatchJobRequest.setExecuteResult(executeResult);
        dispatchJobRequest.setTaskStatus(status);
        dispatchJobRequest.setRetry(jobContext.isRetry());
        dispatchJobRequest.setRetryScene(jobContext.getRetryScene());
        return dispatchJobRequest;
    }
}
