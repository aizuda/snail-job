package com.aizuda.snailjob.server.retry.task.support.dispatch.task;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.RetryLogMetaDTO;
import com.aizuda.snailjob.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.snailjob.server.common.triple.ImmutableTriple;
import com.aizuda.snailjob.server.retry.task.support.RetryContext;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.idempotent.IdempotentHolder;
import com.aizuda.snailjob.server.retry.task.support.idempotent.RetryIdempotentStrategyHandler;
import com.aizuda.snailjob.server.retry.task.support.retry.RetryExecutor;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author opensnail
 * @date 2023-09-23 08:02:17
 * @since 2.4.0
 */
public abstract class AbstractTaskExecutor implements TaskExecutor, InitializingBean {
    protected final RetryIdempotentStrategyHandler idempotentStrategy = IdempotentHolder.getRetryIdempotent();

    @Autowired
    protected SystemProperties systemProperties;
    @Autowired
    protected AccessTemplate accessTemplate;
    @Autowired
    protected ClientNodeAllocateHandler clientNodeAllocateHandler;

    @Override
    public void actuator(RetryTask retryTask) {
        // 重试次数累加
        retryCountIncrement(retryTask);

        RetrySceneConfig retrySceneConfig = accessTemplate.getSceneConfigAccess().getSceneConfigByGroupNameAndSceneName(retryTask.getGroupName(), retryTask.getSceneName(),
                retryTask.getNamespaceId());

        RetryContext retryContext = builderRetryContext(retryTask.getGroupName(), retryTask, retrySceneConfig);
        RetryExecutor executor = builderResultRetryExecutor(retryContext, retrySceneConfig);

        if (!preCheck(retryContext, executor)) {
            return;
        }

        productExecUnitActor(executor);
    }

    protected boolean preCheck(RetryContext retryContext, RetryExecutor executor) {
        Pair<Boolean /*是否符合条件*/, StringBuilder/*描述信息*/> pair = executor.filter();
        if (!pair.getKey()) {
            RetryTask retryTask = retryContext.getRetryTask();
            SnailJobLog.LOCAL.warn("当前任务不满足执行条件. groupName:[{}] uniqueId:[{}], description:[{}]",
                    retryTask.getGroupName(),
                    retryTask.getUniqueId(),
                    pair.getValue().toString());

            RetryLogMetaDTO retryLogMetaDTO = RetryTaskConverter.INSTANCE.toLogMetaDTO(retryTask);
            SnailJobLog.REMOTE.error("触发条件不满足 原因: [{}] <|>{}<|>", pair.getValue().toString(), retryLogMetaDTO);

            return false;
        }

        return true;
    }

    private void retryCountIncrement(RetryTask retryTask) {
        Integer retryCount = retryTask.getRetryCount();
        retryTask.setRetryCount(++retryCount);
    }

    protected void productExecUnitActor(RetryExecutor retryExecutor) {
        RetryTask retryTask = retryExecutor.getRetryContext().getRetryTask();
        String groupName = retryTask.getGroupName();
        String namespaceId = retryTask.getNamespaceId();
        Long retryId = retryExecutor.getRetryContext().getRetryTask().getId();
        idempotentStrategy.set(ImmutableTriple.of(groupName, namespaceId, retryId).toString());

        ActorRef actorRef = getActorRef();
        actorRef.tell(retryExecutor, actorRef);
    }

    protected abstract RetryContext builderRetryContext(String groupName, RetryTask retryTask,
                                                        final RetrySceneConfig retrySceneConfig);

    protected abstract RetryExecutor builderResultRetryExecutor(RetryContext retryContext,
                                                                final RetrySceneConfig retrySceneConfig);

    protected abstract ActorRef getActorRef();

    @Override
    public void afterPropertiesSet() throws Exception {
        TaskActuatorFactory.register(this.getTaskType(), this);
    }
}
