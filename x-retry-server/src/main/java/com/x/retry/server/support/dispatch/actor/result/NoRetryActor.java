package com.x.retry.server.support.dispatch.actor.result;

import akka.actor.AbstractActor;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.server.persistence.mybatis.po.RetryTask;
import com.x.retry.server.persistence.support.RetryTaskAccess;
import com.x.retry.server.support.WaitStrategy;
import com.x.retry.server.support.context.MaxAttemptsPersistenceRetryContext;
import com.x.retry.server.support.retry.RetryExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 不重试,只更新下次触发时间
 *
 * @author: www.byteblogs.com
 * @date : 2022-04-14 16:11
 */
@Component("NoRetryActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class NoRetryActor extends AbstractActor {

    public static final String BEAN_NAME = "NoRetryActor";

    @Autowired
    @Qualifier("retryTaskAccessProcessor")
    private RetryTaskAccess<RetryTask> retryTaskAccess;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryExecutor.class, retryExecutor -> {

            MaxAttemptsPersistenceRetryContext retryContext = (MaxAttemptsPersistenceRetryContext) retryExecutor.getRetryContext();
            RetryTask retryTask = retryContext.getRetryTask();
            WaitStrategy waitStrategy = retryContext.getWaitStrategy();
            retryTask.setNextTriggerAt(waitStrategy.computeRetryTime(retryContext));

            // 不更新重试次数
            retryTask.setRetryCount(null);
            try {
                retryTaskAccess.updateRetryTask(retryTask);
            }catch (Exception e) {
                LogUtils.error(log,"更新重试任务失败", e);
            } finally {
                // 更新DB状态
                getContext().stop(getSelf());
            }

        }).build();
    }

}
