package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.result;

import akka.actor.AbstractActor;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.IdempotentStrategy;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.retry.task.support.event.RetryTaskFailMoreThresholdAlarmEvent;
import com.aizuda.easy.retry.server.retry.task.support.handler.CallbackRetryTaskHandler;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 2.0
 */
@Component(ActorGenerator.FAILURE_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class FailureActor extends AbstractActor {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private AccessTemplate accessTemplate;
    @Autowired
    private CallbackRetryTaskHandler callbackRetryTaskHandler;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private SystemProperties systemProperties;
    @Autowired
    @Qualifier("retryIdempotentStrategyHandler")
    private IdempotentStrategy<Pair<String/*groupName*/, String/*namespaceId*/>, Long> idempotentStrategy;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryTask.class, retryTask -> {
           EasyRetryLog.LOCAL.debug("FailureActor params:[{}]", retryTask);

            try {
                // 超过最大等级
                SceneConfig sceneConfig =
                        accessTemplate.getSceneConfigAccess().getSceneConfigByGroupNameAndSceneName(retryTask.getGroupName(), retryTask.getSceneName(),
                            retryTask.getNamespaceId());

                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {

                        Integer maxRetryCount;
                        if (SyetemTaskTypeEnum.CALLBACK.getType().equals(retryTask.getTaskType())) {
                            maxRetryCount = systemProperties.getCallback().getMaxCount();
                        } else {
                            maxRetryCount = sceneConfig.getMaxRetryCount();
                        }

                        if (maxRetryCount <= retryTask.getRetryCount()) {
                            retryTask.setRetryStatus(RetryStatusEnum.MAX_COUNT.getStatus());
                            // 创建一个回调任务
                            callbackRetryTaskHandler.create(retryTask);
                        }

                        retryTask.setUpdateDt(LocalDateTime.now());
                        Assert.isTrue(1 == accessTemplate.getRetryTaskAccess()
                                        .updateById(retryTask.getGroupName(), retryTask.getNamespaceId(), retryTask),
                                () -> new EasyRetryServerException("更新重试任务失败. groupName:[{}] uniqueId:[{}]",
                                        retryTask.getGroupName(), retryTask.getUniqueId()));

                        // 变动日志的状态
                        RetryTaskLog retryTaskLog = new RetryTaskLog();
                        retryTaskLog.setRetryStatus(retryTask.getRetryStatus());
                        retryTaskLogMapper.update(retryTaskLog, new LambdaUpdateWrapper<RetryTaskLog>()
                            .eq(RetryTaskLog::getNamespaceId, retryTask.getNamespaceId())
                            .eq(RetryTaskLog::getUniqueId, retryTask.getUniqueId())
                            .eq(RetryTaskLog::getGroupName, retryTask.getGroupName()));

                        context.publishEvent(new RetryTaskFailMoreThresholdAlarmEvent(retryTask));
                    }
                });
            } catch (Exception e) {
                EasyRetryLog.LOCAL.error("更新重试任务失败", e);
            } finally {
                // 清除幂等标识位
                idempotentStrategy.clear(Pair.of(retryTask.getGroupName(), retryTask.getNamespaceId()), retryTask.getId());
                getContext().stop(getSelf());
            }

        }).build();

    }

}
