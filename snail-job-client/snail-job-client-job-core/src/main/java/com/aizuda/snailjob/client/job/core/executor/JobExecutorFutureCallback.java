package com.aizuda.snailjob.client.job.core.executor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import com.aizuda.snailjob.client.common.cache.GroupVersionCache;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.log.support.SnailJobLogManager;
import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.job.core.cache.ThreadPoolCache;
import com.aizuda.snailjob.client.job.core.client.JobNettyClient;
import com.aizuda.snailjob.client.job.core.log.JobLogMeta;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.client.model.request.DispatchJobResultRequest;
import com.aizuda.snailjob.common.core.alarm.AlarmContext;
import com.aizuda.snailjob.common.core.alarm.SnailJobAlarmFactory;
import com.aizuda.snailjob.common.core.context.SpringContext;
import com.aizuda.snailjob.common.core.enums.JobNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.JobContext;
import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.util.EnvironmentUtils;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.NetUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.log.enums.LogTypeEnum;
import com.aizuda.snailjob.server.model.dto.ConfigDTO;
import com.aizuda.snailjob.server.model.dto.ConfigDTO.Notify.Recipient;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;

/**
 * @author: opensnail
 * @date : 2023-10-08 16:44
 * @since : 2.4.0
 */
@Slf4j
public class JobExecutorFutureCallback implements FutureCallback<ExecuteResult> {

    private static final String TEXT_MESSAGE_FORMATTER = """
            <font face="微软雅黑" color=#ff0000 size=4>{}环境 定时任务上报异常</font> \s
             > IP:{}   \s
             > 空间ID:{}  \s
             > 名称:{}   \s
             > 时间:{}   \s
             > 异常:{} \s
            \s""";

    private static final JobNettyClient CLIENT = RequestBuilder.<JobNettyClient, NettyResult>newBuilder()
            .client(JobNettyClient.class)
            .callback(nettyResult -> {
                if (nettyResult.getStatus() == StatusEnum.NO.getStatus()) {
                    sendMessage(nettyResult.getMessage());
                }
                SnailJobLog.LOCAL.debug("Job execute result report successfully requestId:[{}]",
                        nettyResult.getRequestId());
            }).build();

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
            SnailJobLog.REMOTE.info("任务执行成功 taskBatchId:[{}] [{}]", jobContext.getTaskBatchId(),
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
            SnailJobLog.REMOTE.error("执行结果上报异常.[{}]", jobContext.getTaskId(), e);
            sendMessage(e.getMessage());
        } finally {
            SnailJobLogManager.removeLogMeta();
            stopThreadPool();
        }
    }

    @Override
    public void onFailure(final Throwable t) {
        ExecuteResult failure = ExecuteResult.failure();
        try {
            // 初始化调度信息（日志上报LogUtil）
            initLogContext();

            // 上报执行失败
            SnailJobLog.REMOTE.error("任务执行失败 taskBatchId:[{}]", jobContext.getTaskBatchId(), t);

            if (t instanceof CancellationException) {
                failure.setMessage("任务被取消");
            } else {
                failure.setMessage(t.getMessage());
            }

            CLIENT.dispatchResult(
                    buildDispatchJobResultRequest(failure, JobTaskStatusEnum.FAIL.getStatus())
            );
        } catch (Exception e) {
            SnailJobLog.REMOTE.error("执行结果上报异常.[{}]", jobContext.getTaskId(), e);
            sendMessage(e.getMessage());
        } finally {
            SnailJobLogManager.removeLogMeta();
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
        SnailJobLogManager.initLogInfo(logMeta, LogTypeEnum.JOB);
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
        // 传递变更后的上下文
        if (CollUtil.isNotEmpty(jobContext.getChangeWfContext())) {
            dispatchJobRequest.setWfContext(JsonUtil.toJsonString(jobContext.getChangeWfContext()));
        }

        return dispatchJobRequest;
    }

    private static void sendMessage(String message) {

        try {
            SnailJobProperties snailJobProperties = SpringContext.getBean(SnailJobProperties.class);
            if (Objects.isNull(snailJobProperties)) {
                return;
            }
            ConfigDTO.Notify notify = GroupVersionCache.getJobNotifyAttribute(
                    JobNotifySceneEnum.JOB_CLIENT_ERROR.getNotifyScene());
            if (Objects.nonNull(notify)) {
                List<Recipient> recipients = Optional.ofNullable(notify.getRecipients()).orElse(Lists.newArrayList());
                for (final Recipient recipient : recipients) {
                    AlarmContext context = AlarmContext.build()
                            .text(TEXT_MESSAGE_FORMATTER,
                                    EnvironmentUtils.getActiveProfile(),
                                    NetUtil.getLocalIpStr(),
                                    snailJobProperties.getNamespace(),
                                    snailJobProperties.getGroup(),
                                    LocalDateTime.now().format(DatePattern.NORM_DATETIME_FORMATTER),
                                    message)
                            .title("定时任务执行结果上报异常:[{}]", snailJobProperties.getGroup())
                            .notifyAttribute(recipient.getNotifyAttribute());

                    Optional.ofNullable(SnailJobAlarmFactory.getAlarmType(recipient.getNotifyType())).ifPresent(alarm -> alarm.asyncSendMessage(context));
                }
            }
        } catch (Exception e1) {
            SnailJobLog.LOCAL.error("Client failed to send component exception alert.", e1);
        }

    }

}
