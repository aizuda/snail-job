package com.aizuda.easy.retry.server.support.dispatch.actor.scan;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.config.SystemProperties;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.server.persistence.support.RetryTaskAccess;
import com.aizuda.easy.retry.server.support.IdempotentStrategy;
import com.aizuda.easy.retry.server.support.RetryContext;
import com.aizuda.easy.retry.server.support.dispatch.DispatchService;
import com.aizuda.easy.retry.server.support.dispatch.ScanTaskDTO;
import com.aizuda.easy.retry.server.support.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.support.retry.RetryExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 数据扫描模板类
 *
 * @author: www.byteblogs.com
 * @date : 2023-06-05 15:44
 * @since 1.5.0
 */
@Slf4j
public abstract class AbstractScanGroup extends AbstractActor {
    @Autowired
    @Qualifier("retryTaskAccessProcessor")
    protected RetryTaskAccess<RetryTask> retryTaskAccessProcessor;
    @Autowired
    @Qualifier("bitSetIdempotentStrategyHandler")
    protected IdempotentStrategy<String, Integer> idempotentStrategy;
    @Autowired
    protected SystemProperties systemProperties;
    @Autowired
    @Qualifier("configAccessProcessor")
    protected ConfigAccess configAccess;
    @Autowired
    protected ClientNodeAllocateHandler clientNodeAllocateHandler;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ScanTaskDTO.class, config -> {

            try {
                doScan(config);
            } catch (Exception e) {
                LogUtils.error(log, "Data scanner processing exception. [{}]", config, e);
            }

        }).build();

    }

    protected void doScan(final ScanTaskDTO scanTaskDTO) {

        LocalDateTime defLastAt = LocalDateTime.now().minusDays(systemProperties.getLastDays());

        String groupName = scanTaskDTO.getGroupName();
        LocalDateTime lastAt = Optional.ofNullable(getLastAt(groupName)).orElse(defLastAt);

        // 扫描当前Group 待重试的数据
        List<RetryTask> list = retryTaskAccessProcessor.listAvailableTasks(groupName, lastAt, systemProperties.getRetryPullPageSize(),
            getTaskType());

        if (!CollectionUtils.isEmpty(list)) {

            // 更新拉取的最大的创建时间
            putLastAt(scanTaskDTO.getGroupName(), list.get(list.size() - 1).getCreateDt());

            for (RetryTask retryTask : list) {

                // 重试次数累加
                retryCountIncrement(retryTask);

                RetryContext retryContext = builderRetryContext(groupName, retryTask);
                RetryExecutor executor = builderResultRetryExecutor(retryContext);

                if (!executor.filter()) {
                    continue;
                }

                productExecUnitActor(executor);
            }
        } else {
            // 数据为空则休眠5s
            try {
                Thread.sleep((DispatchService.PERIOD / 2) * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            putLastAt(groupName, defLastAt);
        }

    }

    protected abstract RetryContext builderRetryContext(String groupName, RetryTask retryTask);

    protected abstract RetryExecutor builderResultRetryExecutor(RetryContext retryContext);

    protected abstract Integer getTaskType();

    protected abstract LocalDateTime getLastAt(String groupName);

    protected abstract LocalDateTime putLastAt(String groupName, LocalDateTime LocalDateTime);

    private void retryCountIncrement(RetryTask retryTask) {
        Integer retryCount = retryTask.getRetryCount();
        retryTask.setRetryCount(++retryCount);
    }

    private void productExecUnitActor(RetryExecutor retryExecutor) {
        String groupIdHash = retryExecutor.getRetryContext().getRetryTask().getGroupName();
        Long retryId = retryExecutor.getRetryContext().getRetryTask().getId();
        idempotentStrategy.set(groupIdHash, retryId.intValue());

        // 重试成功回调客户端
        ActorRef actorRef = getActorRef();
        actorRef.tell(retryExecutor, actorRef);
    }

    protected abstract ActorRef getActorRef();

}
