package com.aizuda.snailjob.server.retry.task.support.dispatch;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.client.model.request.RetryCallbackRequest;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.dto.JobLogMetaDTO;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.dto.RetryLogMetaDTO;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.server.common.rpc.client.SnailJobRetryListener;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.client.RetryRpcClient;
import com.aizuda.snailjob.server.retry.task.dto.RequestCallbackExecutorDTO;
import com.aizuda.snailjob.server.retry.task.dto.RetryExecutorResultDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskLogConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
@Component(ActorGenerator.REAL_CALLBACK_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class RequestCallbackClientActor extends AbstractActor {
    private final RetryTaskMapper retryTaskMapper;
    private final RetryMapper retryMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RequestCallbackExecutorDTO.class, executorDTO -> {
            try {
                doCallback(executorDTO);
            } catch (Exception e) {
                log.error("Client request exception occurred", e);
            }
        }).build();
    }

    private void doCallback(RequestCallbackExecutorDTO executorDTO) {
        long nowMilli = DateUtils.toNowMilli();
        // 检查客户端是否存在
        RegisterNodeInfo registerNodeInfo = CacheRegisterTable.getServerNode(
                executorDTO.getGroupName(),
                executorDTO.getNamespaceId(),
                executorDTO.getClientId()
        );
        if (Objects.isNull(registerNodeInfo)) {
            taskExecuteFailure(executorDTO, "Client does not exist");
            JobLogMetaDTO jobLogMetaDTO = RetryTaskConverter.INSTANCE.toJobLogDTO(executorDTO);
            jobLogMetaDTO.setTimestamp(nowMilli);
            SnailJobLog.REMOTE.error("RetryTaskId:[{}] Task scheduling failed. Reason: No executable client <|>{}<|>", executorDTO.getRetryTaskId(),
                    jobLogMetaDTO);
            return;
        }

        RetryCallbackRequest retryCallbackRequest = RetryTaskConverter.INSTANCE.toRetryCallbackDTO(executorDTO);
        Retry retry = retryMapper.selectOne(new LambdaQueryWrapper<Retry>()
                .select(Retry::getRetryStatus, Retry::getId)
                .eq(Retry::getId, executorDTO.getParentId()));
        if (Objects.isNull(retry)) {
            JobLogMetaDTO jobLogMetaDTO = RetryTaskConverter.INSTANCE.toJobLogDTO(executorDTO);
            jobLogMetaDTO.setTimestamp(nowMilli);
            SnailJobLog.REMOTE.error("RetryTaskId:[{}] Task scheduling failed. Reason: Retry task does not exist <|>{}<|>", executorDTO.getRetryTaskId(),
                    jobLogMetaDTO);
            return;
        }
        retryCallbackRequest.setRetryStatus(retry.getRetryStatus());
        try {

            // 构建请求客户端对象
            RetryRpcClient rpcClient = buildRpcClient(registerNodeInfo, executorDTO);
            Result<Boolean> dispatch = rpcClient.callback(retryCallbackRequest);
            if (dispatch.getStatus() == StatusEnum.YES.getStatus()) {
                SnailJobLog.LOCAL.info("RetryTaskId:[{}] Task scheduled successfully.", executorDTO.getRetryTaskId());
            } else {
                // 客户端返回失败，则认为任务执行失败
                SnailJobLog.LOCAL.error("RetryTaskId:[{}] Task scheduling failed. Msg:[{}]", executorDTO.getRetryTaskId(), dispatch.getMessage());
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
        private final RequestCallbackExecutorDTO executorDTO;

        public RetryExecutorRetryListener(final RequestCallbackExecutorDTO executorDTO) {
            this.executorDTO = executorDTO;
            this.properties = Maps.newHashMap();
        }

        @Override
        public <V> void onRetry(final Attempt<V> attempt) {
            if (attempt.getAttemptNumber() > 1) {
                // 更新最新负载节点
                String hostId = (String) properties.get("HOST_ID");
                String hostIp = (String) properties.get("HOST_IP");
                Integer hostPort = (Integer) properties.get("HOST_PORT");

                RetryTask retryTask = new RetryTask();
                retryTask.setId(executorDTO.getRetryTaskId());
                RegisterNodeInfo realNodeInfo = new RegisterNodeInfo();
                realNodeInfo.setHostIp(hostIp);
                realNodeInfo.setHostPort(Integer.valueOf(hostPort));
                realNodeInfo.setHostId(hostId);
                retryTask.setClientInfo(ClientInfoUtils.generate(realNodeInfo));
                retryTaskMapper.updateById(retryTask);
            }

        }

        @Override
        public Map<String, Object> properties() {
            return properties;
        }
    }

    private RetryRpcClient buildRpcClient(RegisterNodeInfo registerNodeInfo, RequestCallbackExecutorDTO executorDTO) {
        return RequestBuilder.<RetryRpcClient, Result>newBuilder()
                .nodeInfo(registerNodeInfo)
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
     * @param message     失败原因
     */
    private static void taskExecuteFailure(RequestCallbackExecutorDTO executorDTO, String message) {
        ActorRef actorRef = ActorGenerator.retryTaskExecutorResultActor();
        RetryExecutorResultDTO executorResultDTO = RetryTaskConverter.INSTANCE.toRetryExecutorResultDTO(executorDTO);
        executorResultDTO.setExceptionMsg(message);
        executorResultDTO.setTaskStatus(RetryTaskStatusEnum.FAIL.getStatus());
        actorRef.tell(executorResultDTO, actorRef);
    }
}
