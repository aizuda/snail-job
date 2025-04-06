package com.aizuda.snailjob.server.starter.dispatch;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.server.retry.task.support.dispatch.ScanRetryActor;
import  org.apache.pekko.actor.AbstractActor;
import  org.apache.pekko.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheGroupScanActor;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.ScanTask;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.retry.task.support.handler.RateLimiterHandler;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

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
    private final SystemProperties systemProperties;
    private final RateLimiterHandler rateLimiterHandler;

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

        // 刷新最新的配置
        rateLimiterHandler.refreshRate();

        // 通过并行度配置计算拉取范围
        Set<Integer> totalBuckets = consumerBucket.getBuckets();
        int retryMaxPullParallel = systemProperties.getRetryMaxPullParallel();
        List<List<Integer>> partitions = Lists.partition(new ArrayList<>(totalBuckets),
                (totalBuckets.size() + retryMaxPullParallel - 1) / retryMaxPullParallel);
        for (List<Integer> buckets : partitions) {
            String key = StrUtil.join(StrUtil.COMMA, new TreeSet<>(buckets));
            if (ScanRetryActor.REPEATED_PULL.contains(key)) {
                SnailJobLog.LOCAL.warn("Discard the current scanning task because there are ongoing tasks in the current batch.[{}]", key);
                continue;
            }
            ScanTask scanTask = new ScanTask();
            scanTask.setBucketStr(key);
            scanTask.setBuckets(new HashSet<>(buckets));
            ActorRef scanRetryActorRef = ActorGenerator.scanRetryActor();
            scanRetryActorRef.tell(scanTask, scanRetryActorRef);
            ScanRetryActor.REPEATED_PULL.add(key);
        }
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
