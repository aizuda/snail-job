package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.exec;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.client.model.RetryCallbackDTO;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.client.RequestBuilder;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.util.ClientInfoUtils;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.retry.task.client.RetryRpcClient;
import com.aizuda.easy.retry.server.retry.task.dto.LogMetaDTO;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskConverter;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskLogConverter;
import com.aizuda.easy.retry.server.retry.task.support.context.CallbackRetryContext;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.log.RetryTaskLogDTO;
import com.aizuda.easy.retry.server.retry.task.support.handler.CallbackRetryTaskHandler;
import com.aizuda.easy.retry.server.retry.task.support.retry.RetryExecutor;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * 重试结果执行器
 *
 * @author www.byteblogs.com
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
            SceneConfig sceneConfig = context.getSceneConfig();

//            RetryTaskLogDTO retryTaskLog = RetryTaskLogConverter.INSTANCE.toRetryTaskLogDTO(retryTask);
//            retryTaskLog.setTriggerTime(LocalDateTime.now());
//            retryTaskLog.setClientInfo(ClientInfoUtils.generate(serverNode));

            try {

                if (Objects.nonNull(serverNode)) {
                    retryExecutor.call((Callable<Result<Void>>) () -> {
                        Result<Void> result = callClient(retryTask, serverNode, sceneConfig);

                        String message = "回调客户端成功";
                        if (StatusEnum.YES.getStatus() != result.getStatus()) {
                            if (StrUtil.isNotBlank(result.getMessage())) {
                                message = result.getMessage();
                            } else {
                                message = "回调客户端失败: 异常信息为空";
                            }
                        }
//                        retryTaskLog.setMessage(message);
                        return result;
                    });
                    if (context.hasException()) {
//                        retryTaskLog.setMessage(context.getException().getMessage());
                    }
                } else {
//                    retryTaskLog.setMessage("There are currently no available client PODs.");
                }

            } catch (Exception e) {
                LogMetaDTO logMetaDTO = RetryTaskConverter.INSTANCE.toLogMetaDTO(retryTask);
                logMetaDTO.setTimestamp(DateUtils.toNowMilli());
                EasyRetryLog.REMOTE.error("请求客户端异常. <|>{}<|>",  retryTask.getUniqueId(), logMetaDTO, e);//                retryTaskLog.setMessage(StringUtils.isBlank(e.getMessage()) ? StrUtil.EMPTY : e.getMessage());
            } finally {

//                ActorRef actorRef = ActorGenerator.logActor();
//                actorRef.tell(retryTaskLog, actorRef);

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
    private Result callClient(RetryTask callbackTask, RegisterNodeInfo serverNode, SceneConfig sceneConfig) {

        String retryTaskUniqueId = callbackRetryTaskHandler.getRetryTaskUniqueId(callbackTask.getUniqueId());

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        RetryTask retryTask = retryTaskAccess.one(callbackTask.getGroupName(), callbackTask.getNamespaceId(),
                new LambdaQueryWrapper<RetryTask>()
                        .select(RetryTask::getRetryStatus)
                        .eq(RetryTask::getNamespaceId, serverNode.getNamespaceId())
                        .eq(RetryTask::getGroupName, callbackTask.getGroupName())
                        .eq(RetryTask::getUniqueId, retryTaskUniqueId));
        Assert.notNull(retryTask, () -> new EasyRetryServerException("未查询回调任务对应的重试任务. callbackUniqueId:[{}] uniqueId:[{}]",
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
                .namespaceId(serverNode.getNamespaceId())
                .failover(Boolean.TRUE)
                .routeKey(sceneConfig.getRouteKey())
                .allocKey(sceneConfig.getSceneName())
                .executorTimeout(sceneConfig.getExecutorTimeout())
                .client(RetryRpcClient.class)
                .build();
        return rpcClient.callback(retryCallbackDTO);

    }
}
