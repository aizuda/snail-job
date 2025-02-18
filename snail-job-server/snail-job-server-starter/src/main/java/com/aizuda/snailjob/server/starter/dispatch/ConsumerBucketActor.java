package com.aizuda.snailjob.server.starter.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheGroupScanActor;
import com.aizuda.snailjob.server.common.dto.ScanTask;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 消费当前节点分配的bucket并生成扫描任务
 * <p>
 *
 * @author opensnail
 * @date 2023-09-21 23:47:23
 * @since 2.4.0
 */
@Component(ActorGenerator.SCAN_BUCKET_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ConsumerBucketActor extends AbstractActor {
    private static final String DEFAULT_JOB_KEY = "DEFAULT_JOB_KEY";
    private static final String DEFAULT_WORKFLOW_KEY = "DEFAULT_JOB_KEY";
    private static final String DEFAULT_RETRY_KEY = "DEFAULT_RETRY_KEY";

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ConsumerBucket.class, consumerBucket -> {

            try {
                doDispatch(consumerBucket);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("Data dispatcher processing exception. [{}]", consumerBucket, e);
            }

        }).build();
    }

    private void doDispatch(final ConsumerBucket consumerBucket) {
        if (CollUtil.isEmpty(consumerBucket.getBuckets())) {
            return;
        }

        // 扫描job && workflow
        doScanJobAndWorkflow(consumerBucket);

        // 扫描重试
        doScanRetry(consumerBucket);
    }

    private void doScanRetry(final ConsumerBucket consumerBucket) {
        ScanTask scanTask = new ScanTask();
        scanTask.setBuckets(consumerBucket.getBuckets());
        ActorRef scanRetryActorRef = cacheActorRef(DEFAULT_RETRY_KEY, SyetemTaskTypeEnum.RETRY);
        scanRetryActorRef.tell(scanTask, scanRetryActorRef);
    }

    private void doScanJobAndWorkflow(final ConsumerBucket consumerBucket) {
        ScanTask scanTask = new ScanTask();
        scanTask.setBuckets(consumerBucket.getBuckets());

        // 扫描定时任务数据
        ActorRef scanJobActorRef = cacheActorRef(DEFAULT_JOB_KEY, SyetemTaskTypeEnum.JOB);
        scanJobActorRef.tell(scanTask, scanJobActorRef);

        // 扫描DAG工作流任务数据
        ActorRef scanWorkflowActorRef = cacheActorRef(DEFAULT_WORKFLOW_KEY, SyetemTaskTypeEnum.WORKFLOW);
        scanWorkflowActorRef.tell(scanTask, scanWorkflowActorRef);
    }

    /**
     * 缓存Actor对象
     */
    private ActorRef cacheActorRef(String groupName, SyetemTaskTypeEnum typeEnum) {
        ActorRef scanActorRef = CacheGroupScanActor.get(groupName, typeEnum);
        if (Objects.isNull(scanActorRef)) {
            scanActorRef = typeEnum.getActorRef().get();
            // 缓存扫描器actor
            CacheGroupScanActor.put(groupName, typeEnum, scanActorRef);
        }
        return scanActorRef;
    }
}
