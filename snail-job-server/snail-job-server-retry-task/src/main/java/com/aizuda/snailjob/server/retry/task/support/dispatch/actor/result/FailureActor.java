package com.aizuda.snailjob.server.retry.task.support.dispatch.actor.result;

import akka.actor.AbstractActor;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.IdempotentStrategy;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.triple.ImmutableTriple;
import com.aizuda.snailjob.server.retry.task.support.event.RetryTaskFailMoreThresholdAlarmEvent;
import com.aizuda.snailjob.server.retry.task.support.handler.CallbackRetryTaskHandler;
import com.aizuda.snailjob.server.retry.task.support.idempotent.IdempotentHolder;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLog;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * 重试完成执行器 1、更新重试任务 2、记录重试日志
 *
 * @author opensnail
 * @date 2021-10-30
 * @since 2.0
 */
@Component(ActorGenerator.FAILURE_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class FailureActor extends AbstractActor {
    private final IdempotentStrategy<String> idempotentStrategy = IdempotentHolder.getRetryIdempotent();
    private final ApplicationContext context;
    private final AccessTemplate accessTemplate;
    private final CallbackRetryTaskHandler callbackRetryTaskHandler;
    private final TransactionTemplate transactionTemplate;
    private final SystemProperties systemProperties;
    private final RetryTaskLogMapper retryTaskLogMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryTask.class, retryTask -> {
            SnailJobLog.LOCAL.debug("FailureActor params:[{}]", retryTask);

            try {
                // 超过最大等级
                RetrySceneConfig retrySceneConfig =
                        accessTemplate.getSceneConfigAccess().getSceneConfigByGroupNameAndSceneName(retryTask.getGroupName(), retryTask.getSceneName(),
                                retryTask.getNamespaceId());

                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {

                        Integer maxRetryCount;
                        if (SyetemTaskTypeEnum.CALLBACK.getType().equals(retryTask.getTaskType())) {
                            maxRetryCount = systemProperties.getCallback().getMaxCount();
                        } else {
                            maxRetryCount = retrySceneConfig.getMaxRetryCount();
                        }

                        if (maxRetryCount <= retryTask.getRetryCount()) {
                            retryTask.setRetryStatus(RetryStatusEnum.MAX_COUNT.getStatus());
                            // 创建一个回调任务
                            callbackRetryTaskHandler.create(retryTask);
                        }

                        retryTask.setUpdateDt(LocalDateTime.now());
                        Assert.isTrue(1 == accessTemplate.getRetryTaskAccess()
                                        .updateById(retryTask.getGroupName(), retryTask.getNamespaceId(), retryTask),
                                () -> new SnailJobServerException("更新重试任务失败. groupName:[{}] uniqueId:[{}]",
                                        retryTask.getGroupName(), retryTask.getUniqueId()));

                        // 变动日志的状态
                        RetryTaskLog retryTaskLog = new RetryTaskLog();
                        retryTaskLog.setRetryStatus(retryTask.getRetryStatus());
                        retryTaskLogMapper.update(retryTaskLog, new LambdaUpdateWrapper<RetryTaskLog>()
                                .eq(RetryTaskLog::getNamespaceId, retryTask.getNamespaceId())
                                .eq(RetryTaskLog::getUniqueId, retryTask.getUniqueId())
                                .eq(RetryTaskLog::getGroupName, retryTask.getGroupName()));

                        SnailSpringContext.getContext().publishEvent(new RetryTaskFailMoreThresholdAlarmEvent(retryTask));
                    }
                });
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("更新重试任务失败", e);
            } finally {
                // 清除幂等标识位
                idempotentStrategy.clear(
                        ImmutableTriple.of(retryTask.getGroupName(), retryTask.getNamespaceId(), retryTask.getId()).toString());
                getContext().stop(getSelf());
            }

        }).build();

    }

}
