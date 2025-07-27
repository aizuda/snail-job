package com.aizuda.snailjob.server.job.task.support.executor.job;

import com.aizuda.snailjob.server.common.dto.InstanceKey;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import com.aizuda.snailjob.server.common.rpc.client.SnailJobRetryListener;
import com.aizuda.snailjob.server.common.rpc.client.grpc.GrpcClientInvokeHandlerV2;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.model.request.DispatchJobRequest;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.JobNotifySceneEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.dto.JobLogMetaDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.Objects;

/**
 * @author opensnail
 * @date 2023-10-06 16:42:08
 * @since 2.4.0
 */
@Component(ActorGenerator.REAL_JOB_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class RequestClientActor extends AbstractActor {
    private final InstanceManager instanceManager;
    private final JobTaskMapper jobTaskMapper;

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
        InstanceLiveInfo instanceLiveInfo = instanceManager.getInstanceALiveInfoSet(InstanceKey.builder()
                .namespaceId(realJobExecutorDTO.getNamespaceId())
                .groupName(realJobExecutorDTO.getGroupName())
                .hostId(realJobExecutorDTO.getClientId())
                .build());
        if (Objects.isNull(instanceLiveInfo)) {
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
            JobRpcClient rpcClient = buildRpcClient(instanceLiveInfo, realJobExecutorDTO);
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

    public static class JobExecutorRetryListener implements SnailJobRetryListener {

        private final Map<String, Object> properties;
        private final RealJobExecutorDTO realJobExecutorDTO;
        private final JobTaskMapper jobTaskMapper;

        public JobExecutorRetryListener(final RealJobExecutorDTO realJobExecutorDTO, JobTaskMapper jobTaskMapper) {
            this.jobTaskMapper = jobTaskMapper;
            this.realJobExecutorDTO = realJobExecutorDTO;
            properties = Maps.newHashMap();
        }

        @Override
        public Map<String, Object> properties() {
            return properties;
        }

        @Override
        public <V> void onRetry(final Attempt<V> attempt) {
            // 负载节点
            if (attempt.hasException()) {
                JobLogMetaDTO jobLogMetaDTO = JobTaskConverter.INSTANCE.toJobLogDTO(realJobExecutorDTO);
                jobLogMetaDTO.setTimestamp(DateUtils.toNowMilli());
                SnailJobLog.REMOTE.error("Task scheduling failed attempt retry. Task instance ID:[{}] retryCount:[{}]. <|>{}<|>",
                        realJobExecutorDTO.getTaskBatchId(), attempt.getAttemptNumber(), jobLogMetaDTO, attempt.getExceptionCause());
                return;
            }

            // 更新job_task数据
            if (attempt.hasResult() && attempt.getAttemptNumber() > 1) {
                Map<String, Object> properties = properties();
                InstanceLiveInfo instanceLiveInfo = (InstanceLiveInfo) properties.get(GrpcClientInvokeHandlerV2.NEW_INSTANCE_LIVE_INFO);
                if (Objects.nonNull(instanceLiveInfo)) {
                    RegisterNodeInfo nodeInfo = instanceLiveInfo.getNodeInfo();
                    JobTask task = new JobTask();
                    task.setClientInfo(ClientInfoUtils.generate(nodeInfo));
                    task.setId(realJobExecutorDTO.getTaskId());
                    task.setRetryCount((int) attempt.getAttemptNumber());
                    jobTaskMapper.updateById(task);
                }
            }
        }
    }

    private JobRpcClient buildRpcClient(InstanceLiveInfo registerNodeInfo, RealJobExecutorDTO realJobExecutorDTO) {

        int maxRetryTimes = realJobExecutorDTO.getMaxRetryTimes();
        boolean retry = realJobExecutorDTO.getRetryStatus();
        return RequestBuilder.<JobRpcClient, Result>newBuilder()
                .nodeInfo(registerNodeInfo)
                .failover(true)
                .allocKey(String.valueOf(realJobExecutorDTO.getJobId()))
                .routeKey(realJobExecutorDTO.getRouteKey())
                .failRetry(maxRetryTimes > 0 && !retry)
                .retryTimes(maxRetryTimes)
                .retryInterval(realJobExecutorDTO.getRetryInterval())
                .retryListener(new JobExecutorRetryListener(realJobExecutorDTO, jobTaskMapper))
                .client(JobRpcClient.class)
                .targetLabels(realJobExecutorDTO.getLabels())
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
