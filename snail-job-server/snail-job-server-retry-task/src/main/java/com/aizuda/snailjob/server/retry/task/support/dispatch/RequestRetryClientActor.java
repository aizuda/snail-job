package com.aizuda.snailjob.server.retry.task.support.dispatch;

import akka.actor.AbstractActor;
import com.aizuda.snailjob.client.model.DispatchRetryResultDTO;
import com.aizuda.snailjob.client.model.request.DispatchRetryRequest;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.model.SnailJobHeaders;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.dto.JobLogMetaDTO;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.client.RetryRpcClient;
import com.aizuda.snailjob.server.retry.task.dto.RealRetryExecutorDTO;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskLogDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskLogConverter;
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
@Component(ActorGenerator.REAL_RETRY_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class RequestRetryClientActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RealRetryExecutorDTO.class, realRetryExecutorDTO -> {
            try {
                doExecute(realRetryExecutorDTO);
            } catch (Exception e) {
                log.error("请求客户端发生异常", e);
            }
        }).build();
    }

    private void doExecute(RealRetryExecutorDTO executorDTO) {
        long nowMilli = DateUtils.toNowMilli();
        // 检查客户端是否存在
        RegisterNodeInfo registerNodeInfo = CacheRegisterTable.getServerNode(
                executorDTO.getGroupName(),
                executorDTO.getNamespaceId(),
                executorDTO.getClientId()
        );

        if (Objects.isNull(registerNodeInfo)) {
            taskExecuteFailure(executorDTO, "客户端不存在");
            JobLogMetaDTO jobLogMetaDTO = RetryTaskConverter.INSTANCE.toJobLogDTO(executorDTO);
            jobLogMetaDTO.setTimestamp(nowMilli);
            SnailJobLog.REMOTE.error("retryTaskId:[{}] 任务调度失败. 失败原因: 无可执行的客户端 <|>{}<|>", executorDTO.getRetryTaskId(),
                    jobLogMetaDTO);
            return;
        }

        DispatchRetryRequest dispatchJobRequest = RetryTaskConverter.INSTANCE.toDispatchRetryRequest(executorDTO);

        try {

            // 设置header
            SnailJobHeaders snailJobHeaders = new SnailJobHeaders();
            snailJobHeaders.setRetry(Boolean.TRUE);
            snailJobHeaders.setRetryId(String.valueOf(executorDTO.getRetryId()));
            snailJobHeaders.setDdl(executorDTO.getExecutorTimeout());

            // 构建请求客户端对象
            RetryRpcClient rpcClient = buildRpcClient(registerNodeInfo, executorDTO);
            Result<Boolean> dispatch = rpcClient.dispatch(dispatchJobRequest, snailJobHeaders);
            Boolean data = dispatch.getData();
            // todo 是否需要根据DispatchRetryResultDTO
            if (dispatch.getStatus() == StatusEnum.YES.getStatus() && Objects.nonNull(data) && data) {
                SnailJobLog.LOCAL.info("retryTaskId:[{}] 任务调度成功.", executorDTO.getRetryTaskId());
            } else {
                SnailJobLog.LOCAL.error("retryTaskId:[{}] 任务调度失败. msg:[{}]", executorDTO.getRetryTaskId(), dispatch.getMessage());
                // 客户端返回失败，则认为任务执行失败
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

            RetryTaskLogDTO jobLogMetaDTO = RetryTaskLogConverter.INSTANCE.toRetryTaskLogDTO(executorDTO);
//            jobLogMetaDTO.setTimestamp(nowMilli);
//            if (realJobExecutorDTO.getRetryStatus()) {
//                SnailJobLog.REMOTE.error("taskId:[{}] 任务调度失败执行重试 重试次数:[{}]. <|>{}<|>", jobLogMetaDTO.getTaskId(),
//                        realJobExecutorDTO.getRetryCount(), jobLogMetaDTO, throwable);
//            } else {
//                SnailJobLog.REMOTE.error("taskId:[{}] 任务调度失败. <|>{}<|>",
//                        jobLogMetaDTO.getTaskId(),
//                        jobLogMetaDTO, throwable);
//            }

//            taskExecuteFailure(realJobExecutorDTO, throwable.getMessage());
//            SnailSpringContext.getContext().publishEvent(
//                    new JobTaskFailAlarmEvent(JobTaskFailAlarmEventDTO.builder()
//                            .jobTaskBatchId(dispatchJobRequest.getTaskBatchId())
//                            .reason(throwable.getMessage())
//                            .notifyScene(JobNotifySceneEnum.JOB_TASK_ERROR.getNotifyScene())
//                            .build())
//            );
        }

    }

    public static class RetryExecutorRetryListener implements RetryListener {

        private final RealRetryExecutorDTO realRetryExecutorDTO;

        public RetryExecutorRetryListener(final RealRetryExecutorDTO realJobExecutorDTO) {
            this.realRetryExecutorDTO = realJobExecutorDTO;
        }

        @Override
        public <V> void onRetry(final Attempt<V> attempt) {
            // 负载节点
            // todo 重新更新任务的客户端信息
            if (!attempt.hasException()) {
                //
            }
        }
    }

    private RetryRpcClient buildRpcClient(RegisterNodeInfo registerNodeInfo, RealRetryExecutorDTO realRetryExecutorDTO) {
        return RequestBuilder.<RetryRpcClient, Result>newBuilder()
                .nodeInfo(registerNodeInfo)
                .failRetry(true)
                .failover(true)
                .retryTimes(6)
                .retryInterval(1)
                .routeKey(realRetryExecutorDTO.getRouteKey())
                .allocKey(String.valueOf(realRetryExecutorDTO.getRetryTaskId()))
                .retryListener(new RetryExecutorRetryListener(realRetryExecutorDTO))
                .client(RetryRpcClient.class)
                .build();
    }

    private static void taskExecuteFailure(RealRetryExecutorDTO realRetryExecutorDTO, String message) {
//        ActorRef actorRef = ActorGenerator.jobTaskExecutorResultActor();
//        JobExecutorResultDTO jobExecutorResultDTO = JobTaskConverter.INSTANCE.toJobExecutorResultDTO(realRetryExecutorDTO);
//        jobExecutorResultDTO.setTaskStatus(JobTaskStatusEnum.FAIL.getStatus());
//        jobExecutorResultDTO.setMessage(message);
//        actorRef.tell(jobExecutorResultDTO, actorRef);
    }
}
