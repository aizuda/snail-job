package com.aizuda.snailjob.server.retry.task.support.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.enums.RetryOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.dto.RequestCallbackExecutorDTO;
import com.aizuda.snailjob.server.retry.task.dto.RequestRetryExecutorDTO;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskExecuteDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.handler.RetryTaskStopHandler;
import com.aizuda.snailjob.server.retry.task.support.timer.RetryTimeoutCheckTask;
import com.aizuda.snailjob.server.retry.task.support.timer.RetryTimerWheel;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.SceneConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-01-26
 */
@Component(ActorGenerator.RETRY_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class RetryExecutor extends AbstractActor {
    private final RetryMapper retryMapper;
    private final RetryTaskMapper retryTaskMapper;
    private final SceneConfigMapper sceneConfigMapper;
    private final ClientNodeAllocateHandler clientNodeAllocateHandler;
    private final RetryTaskStopHandler retryTaskStopHandler;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryTaskExecuteDTO.class, execute -> {

            try {
                Assert.notNull(execute.getRetryId(), () -> new SnailJobServerException("retryId can not be null"));
                Assert.notNull(execute.getRetryTaskId(), () -> new SnailJobServerException("retryTaskId can not be null"));
                doExecute(execute);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("Data scanner processing exception. [{}]", execute, e);
                updateRetryTaskStatus(execute.getRetryTaskId(), RetryTaskStatusEnum.FAIL.getStatus(), RetryOperationReasonEnum.TASK_EXECUTION_ERROR);
            }

        }).build();
    }

    private void doExecute(RetryTaskExecuteDTO execute) {
        LambdaQueryWrapper<Retry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Retry::getId, execute.getRetryId()).eq(Retry::getRetryStatus, RetryStatusEnum.RUNNING.getStatus());
        Retry retry = retryMapper.selectOne(wrapper);
        if (Objects.isNull(retry)) {
            // 没有执行中的任务不执行调度
            updateRetryTaskStatus(execute.getRetryTaskId(), RetryTaskStatusEnum.CANCEL.getStatus(),  RetryOperationReasonEnum.NOT_RUNNING_RETRY);
            return;
        }

        if (CollUtil.isEmpty(CacheRegisterTable.getServerNodeSet(retry.getGroupName(), retry.getNamespaceId()))) {
            // 无客户端不执行调度
            updateRetryTaskStatus(execute.getRetryTaskId(), RetryTaskStatusEnum.CANCEL.getStatus(), RetryOperationReasonEnum.NOT_CLIENT);
            return;
        }

        RetrySceneConfig retrySceneConfig = sceneConfigMapper.selectOne(new LambdaQueryWrapper<RetrySceneConfig>()
                .eq(RetrySceneConfig::getSceneName, retry.getSceneName())
                .eq(RetrySceneConfig::getGroupName, retry.getGroupName())
                .eq(RetrySceneConfig::getNamespaceId, retry.getNamespaceId())
        );
        if (StatusEnum.NO.getStatus().equals(retrySceneConfig.getSceneStatus())) {
            // 场景已经关闭不执行调度
            updateRetryTaskStatus(execute.getRetryTaskId(), RetryTaskStatusEnum.CANCEL.getStatus(), RetryOperationReasonEnum.NOT_CLIENT);
            return;
        }

        // 获取执行的客户端
        RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(execute.getRetryId().toString(),
                execute.getGroupName(), execute.getNamespaceId(), retrySceneConfig.getRouteKey());
        updateRetryTaskStatus(execute.getRetryTaskId(), RetryTaskStatusEnum.RUNNING.getStatus(),
                ClientInfoUtils.generate(serverNode));

        if (SyetemTaskTypeEnum.CALLBACK.getType().equals(retry.getTaskType())) {
            // 请求客户端
            RequestCallbackExecutorDTO callbackExecutorDTO = RetryTaskConverter.INSTANCE.toRequestCallbackExecutorDTO(retrySceneConfig, retry);
            callbackExecutorDTO.setClientId(serverNode.getHostId());
            callbackExecutorDTO.setRetryTaskId(execute.getRetryTaskId());

            ActorRef actorRef = ActorGenerator.callbackRealTaskExecutorActor();
            actorRef.tell(callbackExecutorDTO, actorRef);
        } else {

            // 请求客户端
            RequestRetryExecutorDTO retryExecutorDTO = RetryTaskConverter.INSTANCE.toRealRetryExecutorDTO(retrySceneConfig, retry);
            retryExecutorDTO.setClientId(serverNode.getHostId());
            retryExecutorDTO.setRetryTaskId(execute.getRetryTaskId());

            ActorRef actorRef = ActorGenerator.retryRealTaskExecutorActor();
            actorRef.tell(retryExecutorDTO, actorRef);
        }

        // 运行中的任务，需要进行超时检查
        RetryTimerWheel.registerWithRetry(() -> new RetryTimeoutCheckTask(
                execute.getRetryTaskId(), execute.getRetryId(), retryTaskStopHandler, retryMapper, retryTaskMapper),
                // 加500ms是为了让尽量保证客户端自己先超时中断，防止客户端上报成功但是服务端已触发超时中断
                Duration.ofMillis(DateUtils.toEpochMilli(retrySceneConfig.getExecutorTimeout()) + 500));
    }

    private void updateRetryTaskStatus(Long retryTaskId, Integer taskStatus, String clientInfo) {
        updateRetryTaskStatus(retryTaskId, taskStatus, RetryOperationReasonEnum.NONE, clientInfo);
    }

    private void updateRetryTaskStatus(Long retryTaskId, Integer taskStatus, RetryOperationReasonEnum reasonEnum) {
        updateRetryTaskStatus(retryTaskId, taskStatus, reasonEnum, null);
    }

    private void updateRetryTaskStatus(Long retryTaskId,
                                       Integer taskStatus,
                                       RetryOperationReasonEnum reasonEnum,
                                       String clientInfo) {
        RetryTask retryTask = new RetryTask();
        retryTask.setId(retryTaskId);
        retryTask.setTaskStatus(taskStatus);
        retryTask.setOperationReason(reasonEnum.getReason());
        retryTask.setClientInfo(clientInfo);
        retryTaskMapper.updateById(retryTask);
    }
}
