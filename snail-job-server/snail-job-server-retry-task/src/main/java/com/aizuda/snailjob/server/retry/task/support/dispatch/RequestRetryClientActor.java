package com.aizuda.snailjob.server.retry.task.support.dispatch;

import com.aizuda.snailjob.server.common.dto.*;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import com.aizuda.snailjob.server.common.rpc.client.grpc.GrpcClientInvokeHandlerV2;
import  org.apache.pekko.actor.AbstractActor;
import  org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.client.model.request.DispatchRetryRequest;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.model.SnailJobHeaders;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.server.common.rpc.client.SnailJobRetryListener;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.client.RetryRpcClient;
import com.aizuda.snailjob.server.retry.task.dto.RequestRetryExecutorDTO;
import com.aizuda.snailjob.server.retry.task.dto.RetryExecutorResultDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskLogConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
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
@Component(ActorGenerator.REAL_RETRY_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class RequestRetryClientActor extends AbstractActor {
    private final RetryTaskMapper retryTaskMapper;
    private final InstanceManager instanceManager;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RequestRetryExecutorDTO.class, realRetryExecutorDTO -> {
            try {
                doExecute(realRetryExecutorDTO);
            } catch (Exception e) {
                log.error("Client request exception occurred", e);
            }
        }).build();
    }

    private void doExecute(RequestRetryExecutorDTO executorDTO) {
        long nowMilli = DateUtils.toNowMilli();
        // 检查客户端是否存在
        InstanceLiveInfo instanceLiveInfo = instanceManager.getInstanceALiveInfoSet(InstanceKey.builder()
                .namespaceId(executorDTO.getNamespaceId())
                .groupName(executorDTO.getGroupName())
                .hostId(executorDTO.getClientId())
                .build());
        if (Objects.isNull(instanceLiveInfo)) {
            taskExecuteFailure(executorDTO, "Client does not exist");
            JobLogMetaDTO jobLogMetaDTO = RetryTaskConverter.INSTANCE.toJobLogDTO(executorDTO);
            jobLogMetaDTO.setTimestamp(nowMilli);
            SnailJobLog.REMOTE.error("RetryTaskId:[{}] Task scheduling failed. Reason: No executable client <|>{}<|>", executorDTO.getRetryTaskId(),
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
            RetryRpcClient rpcClient = buildRpcClient(instanceLiveInfo, executorDTO);
            Result<Boolean> dispatch = rpcClient.dispatch(dispatchJobRequest, snailJobHeaders);
            Boolean data = dispatch.getData();
            if (dispatch.getStatus() == StatusEnum.YES.getStatus() && Objects.nonNull(data) && data) {
                SnailJobLog.LOCAL.info("RetryTaskId:[{}] Task scheduled successfully.", executorDTO.getRetryTaskId());
            } else {
                SnailJobLog.LOCAL.error("RetryTaskId:[{}] Task scheduling failed. Msg:[{}]", executorDTO.getRetryTaskId(), dispatch.getMessage());
                // 客户端返回失败，则认为任务执行失败
                taskExecuteFailure(executorDTO, dispatch.getMessage());
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

            RetryLogMetaDTO retryTaskLogDTO = RetryTaskLogConverter.INSTANCE.toRetryLogMetaDTO(executorDTO);
            retryTaskLogDTO.setTimestamp(nowMilli);
            SnailJobLog.REMOTE.error("RetryTaskId:[{}] Task scheduling failed. <|>{}<|>", retryTaskLogDTO.getRetryTaskId(),
                    retryTaskLogDTO, throwable);

            taskExecuteFailure(executorDTO, throwable.getMessage());
        }

    }

    public class RetryExecutorRetryListener implements SnailJobRetryListener {

        private final Map<String, Object> properties;
        private final RequestRetryExecutorDTO executorDTO;

        public RetryExecutorRetryListener(final RequestRetryExecutorDTO realJobExecutorDTO) {
            this.executorDTO = realJobExecutorDTO;
            this.properties = Maps.newHashMap();
        }

        @Override
        public <V> void onRetry(final Attempt<V> attempt) {
            // 负载节点
            if (attempt.hasException()) {
                JobLogMetaDTO jobLogMetaDTO = RetryTaskConverter.INSTANCE.toJobLogDTO(executorDTO);
                jobLogMetaDTO.setTimestamp(DateUtils.toNowMilli());
                SnailJobLog.REMOTE.error("Task scheduling failed attempt retry. Task instance ID:[{}] retryCount:[{}]. <|>{}<|>",
                        executorDTO.getRetryTaskId(), attempt.getAttemptNumber(), jobLogMetaDTO, attempt.getExceptionCause());
                return;
            }

            // 更新最新负载节点
            if (attempt.hasResult() && attempt.getAttemptNumber() > 1) {
                Map<String, Object> properties = properties();
                InstanceLiveInfo instanceLiveInfo = (InstanceLiveInfo) properties.get(GrpcClientInvokeHandlerV2.NEW_INSTANCE_LIVE_INFO);
                if (Objects.nonNull(instanceLiveInfo)) {
                    RegisterNodeInfo nodeInfo = instanceLiveInfo.getNodeInfo();
                    RetryTask retryTask = new RetryTask();
                    retryTask.setId(executorDTO.getRetryTaskId());
                    RegisterNodeInfo realNodeInfo = new RegisterNodeInfo();
                    realNodeInfo.setHostIp(nodeInfo.getHostIp());
                    realNodeInfo.setHostPort(nodeInfo.getHostPort());
                    realNodeInfo.setHostId(nodeInfo.getHostId());
                    retryTask.setClientInfo(ClientInfoUtils.generate(realNodeInfo));
                    retryTaskMapper.updateById(retryTask);
                }

            }

        }

        @Override
        public Map<String, Object> properties() {
            return properties;
        }
    }

    private RetryRpcClient buildRpcClient(InstanceLiveInfo instanceLiveInfo, RequestRetryExecutorDTO executorDTO) {
        return RequestBuilder.<RetryRpcClient, Result>newBuilder()
                .nodeInfo(instanceLiveInfo)
                .failRetry(true)
                .failover(true)
                .retryTimes(3)
                .retryInterval(1)
                .routeKey(executorDTO.getRouteKey())
                .allocKey(String.valueOf(executorDTO.getRetryTaskId()))
                .retryListener(new RetryExecutorRetryListener(executorDTO))
                .client(RetryRpcClient.class)
                .build();
    }

    /**
     * 更新是执行状态
     *
     * @param executorDTO RequestRetryExecutorDTO
     * @param message 失败原因
     */
    private static void taskExecuteFailure(RequestRetryExecutorDTO executorDTO, String message) {
        ActorRef actorRef = ActorGenerator.retryTaskExecutorResultActor();
        RetryExecutorResultDTO executorResultDTO = RetryTaskConverter.INSTANCE.toRetryExecutorResultDTO(executorDTO);
        executorResultDTO.setExceptionMsg(message);
        executorResultDTO.setTaskStatus(RetryTaskStatusEnum.FAIL.getStatus());
        actorRef.tell(executorResultDTO, actorRef);
    }
}
