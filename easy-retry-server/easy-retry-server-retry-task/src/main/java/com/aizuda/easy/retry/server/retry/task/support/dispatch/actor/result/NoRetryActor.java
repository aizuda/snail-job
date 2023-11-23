package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.result;

import akka.actor.AbstractActor;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.IdempotentStrategy;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.retry.task.support.RetryContext;
import com.aizuda.easy.retry.server.retry.task.support.retry.RetryExecutor;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 不重试,只更新下次触发时间
 *
 * @author: www.byteblogs.com
 * @date : 2022-04-14 16:11
 * @since 1.0.0
 */
@Component(ActorGenerator.NO_RETRY_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class NoRetryActor extends AbstractActor {

    @Autowired
    protected AccessTemplate accessTemplate;
    @Autowired
    @Qualifier("retryIdempotentStrategyHandler")
    private IdempotentStrategy<Pair<String/*groupName*/, String/*namespaceId*/>, Long> idempotentStrategy;

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
                        .updateById(retryTask.getGroupName(),retryTask), () ->
                    new EasyRetryServerException("更新重试任务失败. groupName:[{}] uniqueId:[{}]",
                        retryTask.getGroupName(),  retryTask.getUniqueId()));
            }catch (Exception e) {
                LogUtils.error(log,"更新重试任务失败", e);
            } finally {
                // 清除幂等标识位
                idempotentStrategy.clear(Pair.of(retryTask.getGroupName(), retryTask.getNamespaceId()), retryTask.getId());

                // 更新DB状态
                getContext().stop(getSelf());
            }

        }).build();
    }

}
