package com.aizuda.snailjob.server.retry.task.support.dispatch.actor.result;

import akka.actor.AbstractActor;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.IdempotentStrategy;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.triple.ImmutableTriple;
import com.aizuda.snailjob.server.retry.task.support.RetryContext;
import com.aizuda.snailjob.server.retry.task.support.idempotent.IdempotentHolder;
import com.aizuda.snailjob.server.retry.task.support.retry.RetryExecutor;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 不重试,只更新下次触发时间
 *
 * @author: opensnail
 * @date : 2022-04-14 16:11
 * @since 1.0.0
 */
@Component(ActorGenerator.NO_RETRY_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class NoRetryActor extends AbstractActor {
    private final IdempotentStrategy<String> idempotentStrategy = IdempotentHolder.getRetryIdempotent();
    private final AccessTemplate accessTemplate;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryExecutor.class, retryExecutor -> {

            RetryContext retryContext = retryExecutor.getRetryContext();
            RetryTask retryTask = retryContext.getRetryTask();

            // 不更新重试次数
            retryTask.setRetryCount(null);
            try {
                retryTask.setUpdateDt(LocalDateTime.now());
                Assert.isTrue(1 == accessTemplate.getRetryTaskAccess()
                        .updateById(retryTask.getGroupName(), retryTask.getNamespaceId(), retryTask), () ->
                        new SnailJobServerException("更新重试任务失败. groupName:[{}] uniqueId:[{}]",
                                retryTask.getGroupName(), retryTask.getUniqueId()));
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("更新重试任务失败", e);
            } finally {
                // 清除幂等标识位
                idempotentStrategy.clear(ImmutableTriple.of(retryTask.getGroupName(), retryTask.getNamespaceId(), retryTask.getId()).toString());
                // 更新DB状态
                getContext().stop(getSelf());
            }

        }).build();
    }

}
