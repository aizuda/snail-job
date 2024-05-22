package com.aizuda.snailjob.server.starter.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheConsumerGroup;
import com.aizuda.snailjob.server.common.cache.CacheGroupScanActor;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.ScanTask;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.retry.task.support.cache.CacheGroupRateLimiter;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.cache.Cache;
import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
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
    private final AccessTemplate accessTemplate;
    private final ServerNodeMapper serverNodeMapper;
    private final SystemProperties systemProperties;
    private static final String DEFAULT_JOB_KEY = "DEFAULT_JOB_KEY";
    private static final String DEFAULT_WORKFLOW_KEY = "DEFAULT_JOB_KEY";

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
        List<GroupConfig> groupConfigs = null;
        try {
            // 查询桶对应组信息
            groupConfigs = accessTemplate.getGroupConfigAccess().list(
                    new LambdaQueryWrapper<GroupConfig>()
                            .select(GroupConfig::getGroupName, GroupConfig::getGroupPartition, GroupConfig::getNamespaceId)
                            .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus())
                            .in(GroupConfig::getBucketIndex, consumerBucket.getBuckets())
            );
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("生成重试任务异常.", e);
        }

        if (CollUtil.isNotEmpty(groupConfigs)) {
            for (final GroupConfig groupConfig : groupConfigs) {
                CacheConsumerGroup.addOrUpdate(groupConfig.getGroupName(), groupConfig.getNamespaceId());
                ScanTask scanTask = new ScanTask();
                scanTask.setNamespaceId(groupConfig.getNamespaceId());
                scanTask.setGroupName(groupConfig.getGroupName());
                scanTask.setBuckets(consumerBucket.getBuckets());
                scanTask.setGroupPartition(groupConfig.getGroupPartition());
                produceScanActorTask(scanTask);
            }
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
     * 扫描任务生成器
     *
     * @param scanTask {@link  ScanTask} 组上下文
     */
    private void produceScanActorTask(ScanTask scanTask) {

        String groupName = scanTask.getGroupName();

        // 缓存按照
        cacheRateLimiter(groupName);

        // 扫描重试数据
        ActorRef scanRetryActorRef = cacheActorRef(groupName, SyetemTaskTypeEnum.RETRY);
        scanRetryActorRef.tell(scanTask, scanRetryActorRef);

        // 扫描回调数据
        ActorRef scanCallbackActorRef = cacheActorRef(groupName, SyetemTaskTypeEnum.CALLBACK);
        scanCallbackActorRef.tell(scanTask, scanCallbackActorRef);

    }

    /**
     * 缓存限流对象
     */
    private void cacheRateLimiter(String groupName) {
        List<ServerNode> serverNodes = serverNodeMapper.selectList(new LambdaQueryWrapper<ServerNode>()
                .eq(ServerNode::getGroupName, groupName));
        Cache<String, RateLimiter> rateLimiterCache = CacheGroupRateLimiter.getAll();
        for (ServerNode serverNode : serverNodes) {
            RateLimiter rateLimiter = rateLimiterCache.getIfPresent(serverNode.getHostId());
            if (Objects.isNull(rateLimiter)) {
                rateLimiterCache.put(serverNode.getHostId(), RateLimiter.create(systemProperties.getLimiter()));
            }
        }

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
