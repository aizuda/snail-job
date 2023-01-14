package com.x.retry.server.support.dispatch;

import akka.actor.ActorRef;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.util.concurrent.RateLimiter;
import com.x.retry.common.core.enums.NodeTypeEnum;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.common.core.util.HostUtils;
import com.google.common.cache.Cache;
import com.x.retry.common.core.util.JsonUtil;
import com.x.retry.server.akka.ActorGenerator;
import com.x.retry.server.config.SystemProperties;
import com.x.retry.server.support.allocate.server.AllocateMessageQueueConsistentHash;
import com.x.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.x.retry.server.persistence.mybatis.po.GroupConfig;
import com.x.retry.server.persistence.mybatis.po.ServerNode;
import com.x.retry.server.persistence.support.ConfigAccess;
import com.x.retry.server.support.Lifecycle;
import com.x.retry.server.support.cache.CacheGroupRateLimiter;
import com.x.retry.server.support.cache.CacheGroupScanActor;
import com.x.retry.server.support.handler.ServerRegisterNodeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
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
public class DispatchService implements Lifecycle {

    /**
     * 分配器线程
     */
    private final ScheduledExecutorService dispatchService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r,"DispatchService"));

    /**
     * 缓存待拉取数据的起点时间
     *
     * MAX_ID_MAP[key] = group 的 idHash
     * MAX_ID_MAP[value] = retry_task的 create_at时间
     */
    public static final Map<String, LocalDateTime> LAST_AT_MAP = new HashMap<>();

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

                LogUtils.info("当前节点[{}] 分配的组:[{}]", HostUtils.getIp(), JsonUtil.toJsonString(currentHostGroupList));
                if (!CollectionUtils.isEmpty(currentHostGroupList)) {
                    for (GroupConfig groupConfigContext : currentHostGroupList) {
                        produceScanActorTask(groupConfigContext);
                    }
                }

            } catch (Exception e) {
                LogUtils.error("分发异常", e);
            }


        }, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);
    }

    /**
     * 扫描任务生成器
     *
     * @param groupConfig {@link  GroupConfig} 组上下文
     */
    private void produceScanActorTask(GroupConfig groupConfig) {

        String groupName = groupConfig.getGroupName();

        ActorRef scanActorRef = cacheActorRef(groupName);

        // 缓存按照
        cacheRateLimiter(groupName);

        // rebalance 和 group scan 流程合一
        scanActorRef.tell(groupConfig, scanActorRef);
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
                rateLimiterCache.put(groupName, RateLimiter.create(systemProperties.getLimiter()));
            }
        }

        rateLimiterCache.invalidateAll();

    }

    /**
     * 缓存Actor对象
     */
    private ActorRef cacheActorRef(String groupName) {
        Cache<String, ActorRef> actorRefCache = CacheGroupScanActor.getAll();
        ActorRef scanActorRef = actorRefCache.getIfPresent(groupName);
        if (Objects.isNull(scanActorRef)) {
            scanActorRef = ActorGenerator.scanGroupActor();
            // 缓存扫描器actor
            actorRefCache.put(groupName, scanActorRef);
        }
        return scanActorRef;
    }

    /**
     * 分配当前POD负责的组
     *  RebalanceGroup
     * @return {@link  GroupConfig} 组上下文
     */
    private List<GroupConfig> getCurrentHostGroupList() {


        // TODO 优化点，不能查询所有字段，先查询groupName，分配完成在进行，按照groupName查询组配置列表
        List<GroupConfig> prepareAllocateGroupConfig = configAccess.getAllOpenGroupConfig();
        if (CollectionUtils.isEmpty(prepareAllocateGroupConfig)) {
            return Collections.EMPTY_LIST;
        }
        //为了保证客户端分配算法的一致性,serverNodes 从数据库从数据获取
        List<ServerNode> serverNodes = serverNodeMapper.selectList(new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getNodeType, NodeTypeEnum.SERVER.getType()));

        if (CollectionUtils.isEmpty(serverNodes)) {
            LogUtils.error("服务端节点为空");
            return Collections.EMPTY_LIST;
        }

        List<String> podIdList = serverNodes.stream().map(ServerNode::getHostId).collect(Collectors.toList());

        return new AllocateMessageQueueConsistentHash().allocate(ServerRegisterNodeHandler.CURRENT_CID, prepareAllocateGroupConfig, podIdList);
    }

    @Override
    public void close() {
    }
}
