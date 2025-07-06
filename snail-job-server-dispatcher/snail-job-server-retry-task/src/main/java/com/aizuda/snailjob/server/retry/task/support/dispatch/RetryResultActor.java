package com.aizuda.snailjob.server.retry.task.support.dispatch;

import  org.apache.pekko.actor.AbstractActor;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.retry.task.dto.RetryExecutorResultDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.result.RetryResultContext;
import com.aizuda.snailjob.server.retry.task.support.RetryResultHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-02-02
 */
@Component(ActorGenerator.RETRY_EXECUTOR_RESULT_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class RetryResultActor extends AbstractActor {
    private final List<RetryResultHandler> retryResultHandlers;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryExecutorResultDTO.class, result -> {
            try {
                doResult(result);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("Result processing exception. [{}]", result, e);
            }
        }).build();
    }

    private void doResult(RetryExecutorResultDTO result) {
        RetryResultContext context = RetryTaskConverter.INSTANCE.toRetryResultContext(result);
        for (RetryResultHandler retryResultHandler : retryResultHandlers) {
            if (retryResultHandler.supports(context)) {
                retryResultHandler.handle(context);
            }
        }
    }
}
