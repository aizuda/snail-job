package com.aizuda.snailjob.server.retry.task.support.dispatch;

import com.aizuda.snailjob.server.common.dto.InstanceKey;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import lombok.RequiredArgsConstructor;
import  org.apache.pekko.actor.AbstractActor;
import com.aizuda.snailjob.model.request.StopRetryRequest;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.server.retry.task.client.RetryRpcClient;
import com.aizuda.snailjob.server.retry.task.dto.RequestStopRetryTaskExecutorDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author opensnail
 * @date 2023-10-06 16:42:08
 * @since 2.4.0
 */
@Component(ActorGenerator.RETRY_REAL_STOP_TASK_INSTANCE_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class RequestStopClientActor extends AbstractActor {
    private final InstanceManager instanceManager;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RequestStopRetryTaskExecutorDTO.class, taskExecutorDTO -> {
            try {
                doStop(taskExecutorDTO);
            } catch (Exception e) {
                log.error("Client request exception occurred", e);
            } finally {
                getContext().stop(getSelf());
            }
        }).build();
    }

    private void doStop(RequestStopRetryTaskExecutorDTO executorDTO) {
        // 检查客户端是否存在
        InstanceLiveInfo instanceLiveInfo = instanceManager.getInstanceALiveInfoSet(InstanceKey.builder()
                .namespaceId(executorDTO.getNamespaceId())
                .groupName(executorDTO.getGroupName())
                .hostId(executorDTO.getClientId())
                .build());
        if (Objects.isNull(instanceLiveInfo)) {
            return;
        }

        // 不用关心停止的结果，若服务端尝试终止失败,客户端会兜底进行关闭
        StopRetryRequest stopRetryRequest = RetryTaskConverter.INSTANCE.toStopRetryRequest(executorDTO);

        try {
            // 构建请求客户端对象
            RetryRpcClient rpcClient = buildRpcClient(instanceLiveInfo);
            Result<Boolean> dispatch = rpcClient.stop(stopRetryRequest);
            if (dispatch.getStatus() == StatusEnum.YES.getStatus()) {
                SnailJobLog.LOCAL.info("RetryTaskId:[{}] Task stopped successfully.", executorDTO.getRetryTaskId());
            } else {
                // 客户端返回失败，则认为任务执行失败
                SnailJobLog.LOCAL.warn("RetryTaskId:[{}] Task stop failed.", executorDTO.getRetryTaskId());
            }

        } catch (Exception e) {
            SnailJobLog.LOCAL.error("RetryTaskId:[{}] Task stop failed.", executorDTO.getRetryTaskId(), e);
        }

    }

    private RetryRpcClient buildRpcClient(InstanceLiveInfo instanceLiveInfo) {
        return RequestBuilder.<RetryRpcClient, Result>newBuilder()
                .nodeInfo(instanceLiveInfo)
                .failRetry(true)
                .retryTimes(3)
                .retryInterval(1)
                .client(RetryRpcClient.class)
                .build();
    }
}