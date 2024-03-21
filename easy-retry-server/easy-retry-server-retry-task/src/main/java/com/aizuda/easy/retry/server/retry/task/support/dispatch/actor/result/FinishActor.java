package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.result;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.IdempotentStrategy;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskLogConverter;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.log.RetryTaskLogDTO;
import com.aizuda.easy.retry.server.retry.task.support.handler.CallbackRetryTaskHandler;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * 重试完成执行器
 * 1、更新重试任务
 * 2、记录重试日志
 *
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 2.0
 */
@Component(ActorGenerator.FINISH_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class FinishActor extends AbstractActor {


    @Autowired
    private AccessTemplate accessTemplate;
    @Autowired
    private CallbackRetryTaskHandler callbackRetryTaskHandler;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    @Qualifier("retryIdempotentStrategyHandler")
    private IdempotentStrategy<Pair<String/*groupName*/, String/*namespaceId*/>, Long> idempotentStrategy;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RetryTask.class, retryTask -> {
           EasyRetryLog.LOCAL.debug("FinishActor params:[{}]", retryTask);

            retryTask.setRetryStatus(RetryStatusEnum.FINISH.getStatus());

            try {
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {

                        retryTask.setUpdateDt(LocalDateTime.now());
                        Assert.isTrue(1 == accessTemplate.getRetryTaskAccess()
                                        .updateById(retryTask.getGroupName(), retryTask.getNamespaceId(), retryTask),
                                () -> new EasyRetryServerException("更新重试任务失败. groupName:[{}] uniqueId:[{}]",
                                        retryTask.getGroupName(), retryTask.getUniqueId()));

                        // 创建一个回调任务
                        callbackRetryTaskHandler.create(retryTask);

                        // 变动日志的状态
                        RetryTaskLog retryTaskLog = new RetryTaskLog();
                        retryTaskLog.setRetryStatus(retryTask.getRetryStatus());
                        retryTaskLogMapper.update(retryTaskLog, new LambdaUpdateWrapper<RetryTaskLog>()
                            .eq(RetryTaskLog::getNamespaceId, retryTask.getNamespaceId())
                            .eq(RetryTaskLog::getUniqueId, retryTask.getUniqueId())
                            .eq(RetryTaskLog::getGroupName, retryTask.getGroupName()));

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
