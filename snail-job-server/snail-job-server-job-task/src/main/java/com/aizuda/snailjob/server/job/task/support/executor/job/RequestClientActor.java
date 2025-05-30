package com.aizuda.snailjob.server.job.task.support.executor.job;

import  org.apache.pekko.actor.AbstractActor;
import  org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.client.model.request.DispatchJobRequest;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.JobNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.dto.JobLogMetaDTO;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.client.JobRpcClient;
import com.aizuda.snailjob.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.snailjob.server.job.task.dto.JobTaskFailAlarmEventDTO;
import com.aizuda.snailjob.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.snailjob.server.job.task.support.ClientCallbackHandler;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.alarm.event.JobTaskFailAlarmEvent;
import com.aizuda.snailjob.server.job.task.support.callback.ClientCallbackContext;
import com.aizuda.snailjob.server.job.task.support.callback.ClientCallbackFactory;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

/**
 * @author opensnail
 * @date 2023-10-06 16:42:08
 * @since 2.4.0
 */
@Component(ActorGenerator.REAL_JOB_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class RequestClientActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RealJobExecutorDTO.class, realJobExecutorDTO -> {
            try {
                doExecute(realJobExecutorDTO);
            } catch (Exception e) {
                log.error("Client request exception occurred", e);
            }
        }).build();
    }

    private void doExecute(RealJobExecutorDTO realJobExecutorDTO) {
        long nowMilli = DateUtils.toNowMilli();
        // 检查客户端是否存在
        RegisterNodeInfo registerNodeInfo = CacheRegisterTable.getServerNode(
                realJobExecutorDTO.getGroupName(),
                realJobExecutorDTO.getNamespaceId(),
                realJobExecutorDTO.getClientId());
        if (Objects.isNull(registerNodeInfo)) {
            taskExecuteFailure(realJobExecutorDTO, "Client does not exist");
            JobLogMetaDTO jobLogMetaDTO = JobTaskConverter.INSTANCE.toJobLogDTO(realJobExecutorDTO);
            jobLogMetaDTO.setTimestamp(nowMilli);
            if (realJobExecutorDTO.getRetryStatus()) {
                SnailJobLog.REMOTE.error("Task ID:[{}] Task scheduling failed, executing retry. Reason: No executable client. Retry count:[{}]. <|>{}<|>",
                        realJobExecutorDTO.getTaskId(), realJobExecutorDTO.getRetryCount(), jobLogMetaDTO);
            } else {
                SnailJobLog.REMOTE.error("Task ID:[{}] Task scheduling failed. Reason: No executable client <|>{}<|>", realJobExecutorDTO.getTaskId(),
                        jobLogMetaDTO);
            }
            return;
        }

        DispatchJobRequest dispatchJobRequest = JobTaskConverter.INSTANCE.toDispatchJobRequest(realJobExecutorDTO);

        // 兼容历史客户端版本正式版本即可删除
        dispatchJobRequest.setRetry(realJobExecutorDTO.getRetryStatus());

        try {
            // 构建请求客户端对象
            JobRpcClient rpcClient = buildRpcClient(registerNodeInfo, realJobExecutorDTO);
            Result<Boolean> dispatch = rpcClient.dispatch(dispatchJobRequest);
            if (dispatch.getStatus() == StatusEnum.YES.getStatus() && Objects.equals(dispatch.getData(), Boolean.TRUE)) {
                SnailJobLog.LOCAL.info("Task ID:[{}] Task scheduled successfully.", realJobExecutorDTO.getTaskId());
            } else {
                // 客户端返回失败，则认为任务执行失败
                ClientCallbackHandler clientCallback = ClientCallbackFactory.getClientCallback(realJobExecutorDTO.getTaskType());
                ClientCallbackContext context = JobTaskConverter.INSTANCE.toClientCallbackContext(realJobExecutorDTO);
                context.setTaskStatus(JobTaskStatusEnum.FAIL.getStatus());
                context.setExecuteResult(ExecuteResult.failure(null, dispatch.getMessage()));
                clientCallback.callback(context);
            }

        } catch (Exception e) {
            Throwable throwable;
            if (e.getClass().isAssignableFrom(RetryException.class)) {
                RetryException re = (RetryException) e;
                throwable = re.getLastFailedAttempt().getExceptionCause();
            } else if (e.getClass().isAssignableFrom(UndeclaredThrowableException.class)) {
                UndeclaredThrowableException re = (UndeclaredThrowableException) e;
                throwable = re.getUndeclaredThrowable();
            } else {
                throwable = e;
            }

            JobLogMetaDTO jobLogMetaDTO = JobTaskConverter.INSTANCE.toJobLogDTO(realJobExecutorDTO);
            jobLogMetaDTO.setTimestamp(nowMilli);
            if (realJobExecutorDTO.getRetryStatus()) {
                SnailJobLog.REMOTE.error("Task ID:[{}] Task scheduling failed, executing retry. Retry count:[{}]. <|>{}<|>", jobLogMetaDTO.getTaskId(),
                        realJobExecutorDTO.getRetryCount(), jobLogMetaDTO, throwable);
            } else {
                SnailJobLog.REMOTE.error("Task ID:[{}] Task scheduling failed. <|>{}<|>",
                        jobLogMetaDTO.getTaskId(),
                        jobLogMetaDTO, throwable);
            }

            taskExecuteFailure(realJobExecutorDTO, throwable.getMessage());
            SnailSpringContext.getContext().publishEvent(
                    new JobTaskFailAlarmEvent(JobTaskFailAlarmEventDTO.builder()
                            .jobTaskBatchId(dispatchJobRequest.getTaskBatchId())
                            .reason(throwable.getMessage())
                            .notifyScene(JobNotifySceneEnum.JOB_TASK_ERROR.getNotifyScene())
                            .build()));
        }

    }

    public static class JobExecutorRetryListener implements RetryListener {

        private final RealJobExecutorDTO realJobExecutorDTO;

        public JobExecutorRetryListener(final RealJobExecutorDTO realJobExecutorDTO) {
            this.realJobExecutorDTO = realJobExecutorDTO;
        }

        @Override
        public <V> void onRetry(final Attempt<V> attempt) {
            // 负载节点
            if (attempt.hasException()) {
                SnailJobLog.LOCAL.error("Task scheduling failed. Task instance ID:[{}] Count:[{}]",
                        realJobExecutorDTO.getTaskBatchId(), attempt.getAttemptNumber(), attempt.getExceptionCause());
                ClientCallbackHandler clientCallback = ClientCallbackFactory.getClientCallback(realJobExecutorDTO.getTaskType());
                ClientCallbackContext context = JobTaskConverter.INSTANCE.toClientCallbackContext(realJobExecutorDTO);
                context.setTaskStatus(JobTaskStatusEnum.FAIL.getStatus());
                context.setExecuteResult(ExecuteResult.failure(null, "Network request failed"));
                clientCallback.callback(context);
            }
        }
    }

    private JobRpcClient buildRpcClient(RegisterNodeInfo registerNodeInfo, RealJobExecutorDTO realJobExecutorDTO) {

        int maxRetryTimes = realJobExecutorDTO.getMaxRetryTimes();
        boolean retry = realJobExecutorDTO.getRetryStatus();
        return RequestBuilder.<JobRpcClient, Result>newBuilder()
                .nodeInfo(registerNodeInfo)
                .failRetry(maxRetryTimes > 0 && !retry)
                .retryTimes(maxRetryTimes)
                .retryInterval(realJobExecutorDTO.getRetryInterval())
                .retryListener(new JobExecutorRetryListener(realJobExecutorDTO))
                .client(JobRpcClient.class)
                .build();
    }

    private static void taskExecuteFailure(RealJobExecutorDTO realJobExecutorDTO, String message) {
        ActorRef actorRef = ActorGenerator.jobTaskExecutorResultActor();
        JobExecutorResultDTO jobExecutorResultDTO = JobTaskConverter.INSTANCE.toJobExecutorResultDTO(realJobExecutorDTO);
        jobExecutorResultDTO.setTaskStatus(JobTaskStatusEnum.FAIL.getStatus());
        jobExecutorResultDTO.setMessage(message);
        actorRef.tell(jobExecutorResultDTO, actorRef);
    }
}
