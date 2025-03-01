package com.aizuda.snailjob.server.retry.task.support.dispatch.actor.exec;

import akka.actor.AbstractActor;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.client.model.RetryCallbackDTO;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.dto.RetryLogMetaDTO;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.server.retry.task.client.RetryRpcClient;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.context.CallbackRetryContext;
import com.aizuda.snailjob.server.retry.task.support.handler.CallbackRetryTaskHandler;
import com.aizuda.snailjob.server.retry.task.support.retry.RetryExecutor;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 重试结果执行器
 *
 * @author opensnail
 * @date 2021-10-30
 * @since 1.5.0
 */
@Component(ActorGenerator.EXEC_CALLBACK_UNIT_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ExecCallbackUnitActor extends AbstractActor {

    @Autowired
    private AccessTemplate accessTemplate;
    @Autowired
    private CallbackRetryTaskHandler callbackRetryTaskHandler;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryExecutor.class, retryExecutor -> {

            CallbackRetryContext context = (CallbackRetryContext) retryExecutor.getRetryContext();
            RetryTask retryTask = context.getRetryTask();
            RegisterNodeInfo serverNode = context.getServerNode();
            RetrySceneConfig retrySceneConfig = context.getRetrySceneConfig();

//            RetryTaskLogDTO retryTaskLog = RetryTaskLogConverter.INSTANCE.toRetryTaskLogDTO(retryTask);
//            retryTaskLog.setTriggerTime(LocalDateTime.now());
//            retryTaskLog.setClientInfo(ClientInfoUtils.generate(serverNode));

            try {

                if (Objects.nonNull(serverNode)) {
                    retryExecutor.call((Callable<Result<Void>>) () -> {
                        Result<Void> result = callClient(retryTask, serverNode, retrySceneConfig);
                        return result;
                    });
                }

            } catch (Exception e) {
                RetryLogMetaDTO retryLogMetaDTO = RetryTaskConverter.INSTANCE.toLogMetaDTO(retryTask);
                SnailJobLog.REMOTE.error("请求客户端异常. <|>{}<|>", retryTask.getUniqueId(), retryLogMetaDTO, e);
            } finally {
                getContext().stop(getSelf());

            }

        }).build();
    }

    /**
     * 调用客户端
     *
     * @param callbackTask {@link RetryTask} 回调任务
     * @return 重试结果返回值
     */
    private Result callClient(RetryTask callbackTask, RegisterNodeInfo serverNode, RetrySceneConfig retrySceneConfig) {

        String retryTaskUniqueId = callbackRetryTaskHandler.getRetryTaskUniqueId(callbackTask.getUniqueId());

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        RetryTask retryTask = retryTaskAccess.one(callbackTask.getGroupName(), callbackTask.getNamespaceId(),
                new LambdaQueryWrapper<RetryTask>()
                        .select(RetryTask::getRetryStatus)
                        .eq(RetryTask::getNamespaceId, serverNode.getNamespaceId())
                        .eq(RetryTask::getGroupName, callbackTask.getGroupName())
                        .eq(RetryTask::getUniqueId, retryTaskUniqueId));
        Assert.notNull(retryTask, () -> new SnailJobServerException("未查询回调任务对应的重试任务. callbackUniqueId:[{}] uniqueId:[{}]",
                callbackTask.getUniqueId(), retryTaskUniqueId));

        // 回调参数
        RetryCallbackDTO retryCallbackDTO = new RetryCallbackDTO();
        retryCallbackDTO.setIdempotentId(callbackTask.getIdempotentId());
        // 重试任务的状态, 客户端根据重试状态判断最大次数或者成功成功
        retryCallbackDTO.setRetryStatus(retryTask.getRetryStatus());
        retryCallbackDTO.setArgsStr(callbackTask.getArgsStr());
        retryCallbackDTO.setScene(callbackTask.getSceneName());
        retryCallbackDTO.setGroup(callbackTask.getGroupName());
        retryCallbackDTO.setExecutorName(callbackTask.getExecutorName());
        retryCallbackDTO.setUniqueId(callbackTask.getUniqueId());
        retryCallbackDTO.setNamespaceId(callbackTask.getNamespaceId());

        RetryRpcClient rpcClient = RequestBuilder.<RetryRpcClient, Result>newBuilder()
                .nodeInfo(serverNode)
                .failover(Boolean.TRUE)
                .routeKey(retrySceneConfig.getRouteKey())
                .allocKey(retrySceneConfig.getSceneName())
                .executorTimeout(retrySceneConfig.getExecutorTimeout())
                .client(RetryRpcClient.class)
                .build();
        return rpcClient.callback(retryCallbackDTO);

    }
}
