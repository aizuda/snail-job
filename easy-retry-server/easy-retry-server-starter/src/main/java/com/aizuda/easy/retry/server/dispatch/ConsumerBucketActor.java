package com.aizuda.easy.retry.server.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.common.cache.CacheGroupScanActor;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.common.dto.ScanTask;
import com.aizuda.easy.retry.server.retry.task.support.cache.CacheGroupRateLimiter;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.cache.Cache;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消费当前节点分配的bucket并生成扫描任务
 * <p>
 *
 * @author www.byteblogs.com
 * @date 2023-09-21 23:47:23
 * @since 2.4.0
 */
@Component(ActorGenerator.SCAN_BUCKET_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ConsumerBucketActor extends AbstractActor {

    @Autowired
    protected AccessTemplate accessTemplate;
    @Autowired
    protected ServerNodeMapper serverNodeMapper;
    @Autowired
    protected SystemProperties systemProperties;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ConsumerBucket.class, consumerBucket -> {

            try {
                doDispatch(consumerBucket);
            } catch (Exception e) {
                LogUtils.error(log, "Data dispatcher processing exception. [{}]", consumerBucket, e);
            }

        }).build();
    }

    private void doDispatch(final ConsumerBucket consumerBucket) {

        // 查询桶对应组信息
        Set<String> groupNameSet = accessTemplate.getSceneConfigAccess().list(
            new LambdaQueryWrapper<SceneConfig>().select(SceneConfig::getGroupName)
                .eq(SceneConfig::getSceneStatus, StatusEnum.YES.getStatus())
                .in(SceneConfig::getBucketIndex, consumerBucket.getBuckets())
                .groupBy(SceneConfig::getGroupName)).stream().map(SceneConfig::getGroupName).collect(Collectors.toSet());

        CacheConsumerGroup.clear();
        // todo 需要对groupNameSet进行状态过滤只有开启才进行任务调度
        for (final String groupName : groupNameSet) {
            CacheConsumerGroup.addOrUpdate(groupName);
            ScanTask scanTask = new ScanTask();
            scanTask.setBuckets(consumerBucket.getBuckets());
            scanTask.setGroupName(groupName);
            produceScanActorTask(scanTask);
        }

        // job
        ScanTask scanTask = new ScanTask();
        scanTask.setBuckets(consumerBucket.getBuckets());
        ActorRef jobActor = TaskTypeEnum.JOB.getActorRef().get();
        jobActor.tell(scanTask, jobActor);
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
        ActorRef scanRetryActorRef = cacheActorRef(groupName, TaskTypeEnum.RETRY);
        scanRetryActorRef.tell(scanTask, scanRetryActorRef);

        // 扫描回调数据
        ActorRef scanCallbackActorRef = cacheActorRef(groupName, TaskTypeEnum.CALLBACK);
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
    private ActorRef cacheActorRef(String groupName, TaskTypeEnum typeEnum) {
        ActorRef scanActorRef = CacheGroupScanActor.get(groupName, typeEnum);
        if (Objects.isNull(scanActorRef)) {
            scanActorRef = typeEnum.getActorRef().get();
            // 缓存扫描器actor
            CacheGroupScanActor.put(groupName, typeEnum, scanActorRef);
        }
        return scanActorRef;
    }
}
