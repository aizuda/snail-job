package com.aizuda.easy.retry.server.support.dispatch;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.config.SystemProperties;
import com.aizuda.easy.retry.server.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.support.Lifecycle;
import com.aizuda.easy.retry.server.support.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.support.cache.CacheGroupRateLimiter;
import com.aizuda.easy.retry.server.support.cache.CacheGroupScanActor;
import com.aizuda.easy.retry.server.support.handler.ServerNodeBalance;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.cache.Cache;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 分发器组件
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-19 15:46
 */
@Component
@Slf4j
public class DispatchService implements Lifecycle {

    /**
     * 分配器线程
     */
    private final ScheduledExecutorService dispatchService = Executors
        .newSingleThreadScheduledExecutor(r -> new Thread(r, "DispatchService"));

    /**
     * 调度时长
     */
    public static final Long PERIOD = 10L;

    /**
     * 延迟10s为了尽可能保障集群节点都启动完成在进行rebalance
     */
    public static final Long INITIAL_DELAY = 10L;

    @Autowired
    private ServerNodeMapper serverNodeMapper;

    @Autowired
    private SystemProperties systemProperties;

    @Override
    public void start() {

        dispatchService.scheduleAtFixedRate(() -> {

            try {
                Set<String> currentHostGroupList = getCurrentHostGroupList();
                if (!CollectionUtils.isEmpty(currentHostGroupList)) {
                    for (String groupName : currentHostGroupList) {
                        ScanTaskDTO scanTaskDTO = new ScanTaskDTO();
                        scanTaskDTO.setGroupName(groupName);
                        produceScanActorTask(scanTaskDTO);
                    }
                }

            } catch (Exception e) {
                LogUtils.error(log, "分发异常", e);
            }


        }, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);
    }

    /**
     * 扫描任务生成器
     *
     * @param scanTaskDTO {@link  GroupConfig} 组上下文
     */
    private void produceScanActorTask(ScanTaskDTO scanTaskDTO) {

        String groupName = scanTaskDTO.getGroupName();

        // 缓存按照
        cacheRateLimiter(groupName);

        // 扫描重试数据
        ActorRef scanRetryActorRef = cacheActorRef(groupName, TaskTypeEnum.RETRY);
        scanRetryActorRef.tell(scanTaskDTO, scanRetryActorRef);

        // 扫描回调数据
        ActorRef scanCallbackActorRef = cacheActorRef(groupName, TaskTypeEnum.CALLBACK);
        scanCallbackActorRef.tell(scanTaskDTO, scanCallbackActorRef);

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
            scanActorRef = typeEnum.getActorRef();
            // 缓存扫描器actor
            CacheGroupScanActor.put(groupName, typeEnum, scanActorRef);
        }
        return scanActorRef;
    }

    /**
     * 分配当前POD负责的组 RebalanceGroup
     *
     * @return {@link  GroupConfig} 组上下文
     */
    private Set<String> getCurrentHostGroupList() {
        return CacheConsumerGroup.getAllPods();
    }

    @Override
    public void close() {
    }
}
