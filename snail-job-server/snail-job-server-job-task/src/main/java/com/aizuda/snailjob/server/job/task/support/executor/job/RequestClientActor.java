package com.aizuda.snailjob.server.job.task.support.executor.job;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.client.model.request.DispatchJobRequest;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.dto.JobLogMetaDTO;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.client.JobRpcClient;
import com.aizuda.snailjob.server.job.task.dto.JobExecutorResultDTO;
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
                log.error("请求客户端发生异常", e);
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
            taskExecuteFailure(realJobExecutorDTO, "客户端不存在");
            JobLogMetaDTO jobLogMetaDTO = JobTaskConverter.INSTANCE.toJobLogDTO(realJobExecutorDTO);
            jobLogMetaDTO.setTimestamp(nowMilli);
            if (realJobExecutorDTO.isRetry()) {
                SnailJobLog.REMOTE.error("taskId:[{}] 任务调度失败执行重试. 失败原因: 无可执行的客户端. 重试次数:[{}]. <|>{}<|>",
                        realJobExecutorDTO.getTaskId(), realJobExecutorDTO.getRetryCount(), jobLogMetaDTO);
            } else {
                SnailJobLog.REMOTE.error("taskId:[{}] 任务调度失败. 失败原因: 无可执行的客户端 <|>{}<|>", realJobExecutorDTO.getTaskId(),
                        jobLogMetaDTO);
            }
            return;
        }

        DispatchJobRequest dispatchJobRequest = JobTaskConverter.INSTANCE.toDispatchJobRequest(realJobExecutorDTO);

        try {
            // 构建请求客户端对象
            JobRpcClient rpcClient = buildRpcClient(registerNodeInfo, realJobExecutorDTO);
            Result<Boolean> dispatch = rpcClient.dispatch(dispatchJobRequest);
            if (dispatch.getStatus() == StatusEnum.YES.getStatus() && Objects.equals(dispatch.getData(), Boolean.TRUE)) {
                SnailJobLog.LOCAL.info("taskId:[{}] 任务调度成功.", realJobExecutorDTO.getTaskId());
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
            if (realJobExecutorDTO.isRetry()) {
                SnailJobLog.REMOTE.error("taskId:[{}] 任务调度失败执行重试 重试次数:[{}]. <|>{}<|>", jobLogMetaDTO.getTaskId(),
                        realJobExecutorDTO.getRetryCount(), jobLogMetaDTO, throwable);
            } else {
                SnailJobLog.REMOTE.error("taskId:[{}] 任务调度失败. <|>{}<|>", jobLogMetaDTO.getTaskId(),
                        jobLogMetaDTO, throwable);
            }

            taskExecuteFailure(realJobExecutorDTO, throwable.getMessage());
            SnailSpringContext.getContext().publishEvent(new JobTaskFailAlarmEvent(dispatchJobRequest.getTaskBatchId()));
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
                SnailJobLog.LOCAL.error("任务调度失败. taskInstanceId:[{}] count:[{}]",
                        realJobExecutorDTO.getTaskBatchId(), attempt.getAttemptNumber(), attempt.getExceptionCause());
                ClientCallbackHandler clientCallback = ClientCallbackFactory.getClientCallback(realJobExecutorDTO.getTaskType());
                ClientCallbackContext context = JobTaskConverter.INSTANCE.toClientCallbackContext(realJobExecutorDTO);
                context.setTaskStatus(JobTaskStatusEnum.FAIL.getStatus());
                context.setExecuteResult(ExecuteResult.failure(null, "网络请求失败"));
                clientCallback.callback(context);
            }
        }
    }

    private JobRpcClient buildRpcClient(RegisterNodeInfo registerNodeInfo, RealJobExecutorDTO realJobExecutorDTO) {

        int maxRetryTimes = realJobExecutorDTO.getMaxRetryTimes();
        boolean retry = realJobExecutorDTO.isRetry();
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
