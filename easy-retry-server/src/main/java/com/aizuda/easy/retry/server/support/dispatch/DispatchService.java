package com.aizuda.easy.retry.server.support.dispatch;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.server.enums.TaskTypeEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.akka.ActorGenerator;
import com.aizuda.easy.retry.server.config.SystemProperties;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.server.support.Lifecycle;
import com.aizuda.easy.retry.server.support.allocate.server.AllocateMessageQueueConsistentHash;
import com.aizuda.easy.retry.server.support.cache.CacheGroupRateLimiter;
import com.aizuda.easy.retry.server.support.cache.CacheGroupScanActor;
import com.aizuda.easy.retry.server.support.handler.ServerRegisterNodeHandler;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.cache.Cache;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;

    @Autowired
    private SystemProperties systemProperties;

    @Override
    public void start() {

        dispatchService.scheduleAtFixedRate(() -> {

            try {
                List<GroupConfig> currentHostGroupList = getCurrentHostGroupList();
                if (!CollectionUtils.isEmpty(currentHostGroupList)) {
                    for (GroupConfig groupConfigContext : currentHostGroupList) {
                        ScanTaskDTO scanTaskDTO = new ScanTaskDTO();
                        scanTaskDTO.setGroupName(groupConfigContext.getGroupName());
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
    private List<GroupConfig> getCurrentHostGroupList() {
        List<GroupConfig> prepareAllocateGroupConfig = configAccess.getAllOpenGroupConfig();
        if (CollectionUtils.isEmpty(prepareAllocateGroupConfig)) {
            return Collections.EMPTY_LIST;
        }
        //为了保证客户端分配算法的一致性,serverNodes 从数据库从数据获取
        List<ServerNode> serverNodes = serverNodeMapper.selectList(
            new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getNodeType, NodeTypeEnum.SERVER.getType()));

        if (CollectionUtils.isEmpty(serverNodes)) {
            LogUtils.error(log, "服务端节点为空");
            return Collections.EMPTY_LIST;
        }

        List<String> podIdList = serverNodes.stream().map(ServerNode::getHostId).collect(Collectors.toList());

        return new AllocateMessageQueueConsistentHash()
            .allocate(ServerRegisterNodeHandler.CURRENT_CID, prepareAllocateGroupConfig, podIdList);
    }

    @Override
    public void close() {
    }
}
