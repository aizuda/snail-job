package com.aizuda.easy.retry.server.support.handler;

import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.server.support.Lifecycle;
import com.aizuda.easy.retry.server.support.allocate.server.AllocateMessageQueueConsistentHash;
import com.aizuda.easy.retry.server.support.cache.CacheConsumerGroup;
import com.aizuda.easy.retry.server.support.cache.CacheGroup;
import com.aizuda.easy.retry.server.support.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.support.register.ServerRegister;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 负责处理组或者节点变化时，重新分配组在不同的节点上消费
 *
 * @author: www.byteblogs.com
 * @date : 2023-06-08 15:58
 * @since 1.6.0
 */
@Component
@Slf4j
public class ServerNodeBalance implements Lifecycle, Runnable {

    /**
     * 延迟10s为了尽可能保障集群节点都启动完成在进行rebalance
     */
    public static final Long INITIAL_DELAY = 10L;

    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;
    private Thread THREAD = null;

    @Autowired
    protected ServerNodeMapper serverNodeMapper;

    /**
     * 控制rebalance状态
     */
    public static final AtomicBoolean RE_BALANCE_ING = new AtomicBoolean(Boolean.FALSE);

    public void doBalance() {
        LogUtils.info(log, "rebalance start");
        RE_BALANCE_ING.set(Boolean.TRUE);

        try {

            // 为了保证客户端分配算法的一致性,serverNodes 从数据库从数据获取
            Set<String> podIpSet = CacheRegisterTable.getPodIdSet(ServerRegister.GROUP_NAME);

            if (CollectionUtils.isEmpty(podIpSet)) {
                LogUtils.error(log, "server node is empty");
                return;
            }

            Set<String> allGroup = CacheGroup.getAllGroup();
            if (CollectionUtils.isEmpty(allGroup)) {
                LogUtils.error(log, "group is empty");
                return;
            }

            List<String> allocate = new AllocateMessageQueueConsistentHash()
                    .allocate(ServerRegister.CURRENT_CID, new ArrayList<>(allGroup), new ArrayList<>(podIpSet));

            // 删除本地缓存的所有组信息
            CacheConsumerGroup.clear();
            // 重新覆盖本地分配的组信息
            for (String groupName : allocate) {
                CacheConsumerGroup.addOrUpdate(groupName);
            }

            LogUtils.info(log, "rebalance complete. allocate:[{}]", allocate);
        } catch (Exception e) {
            LogUtils.error(log, "rebalance error. ", e);
        } finally {
            RE_BALANCE_ING.set(Boolean.FALSE);
        }

    }

    @Override
    public void start() {
        LogUtils.info(log, "ServerNodeBalance start");
        THREAD = new Thread(this, "server-node-balance");
        THREAD.start();
    }

    private void removeNode(ConcurrentMap<String, RegisterNodeInfo> concurrentMap, Set<String> remoteHostIds, Set<String> localHostIds) {

        localHostIds.removeAll(remoteHostIds);
        for (String localHostId : localHostIds) {
            RegisterNodeInfo registerNodeInfo = concurrentMap.get(localHostId);
            // 删除过期的节点信息
            CacheRegisterTable.remove(registerNodeInfo.getGroupName(), registerNodeInfo.getHostId());
            // 删除本地消费组信息
            CacheConsumerGroup.remove(registerNodeInfo.getGroupName());
        }
    }

    private void refreshAndRemoveGroup(List<GroupConfig> removeGroupConfig, final Set<String> allGroup) {
        if (allGroup.size() != removeGroupConfig.size()) {
            allGroup.removeAll(removeGroupConfig.stream()
                    .map(GroupConfig::getGroupName).collect(Collectors.toSet()));

            // 删除已关闭的组
            for (String groupName : allGroup) {
                CacheGroup.remove(groupName);
            }

            // 添加组
            for (final GroupConfig groupConfig : removeGroupConfig) {
                CacheGroup.addOrUpdate(groupConfig.getGroupName());
            }
        }
    }

    private void refreshCache(List<ServerNode> remotePods) {

        // 刷新最新的节点注册信息
        for (ServerNode node : remotePods) {
            CacheRegisterTable.addOrUpdate(node.getGroupName(), node);
        }
    }

    @Override
    public void close() {

        // 停止定时任务
        THREAD.interrupt();

        LogUtils.info(log, "ServerNodeBalance start. ");
        int i = serverNodeMapper
                .delete(new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getHostId, ServerRegister.CURRENT_CID));
        if (1 == i) {
            LogUtils.info(log, "delete node success. [{}]", ServerRegister.CURRENT_CID);
        } else {
            LogUtils.info(log, "delete node  error. [{}]", ServerRegister.CURRENT_CID);
        }

        LogUtils.info(log, "ServerNodeBalance close complete");
    }

    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(INITIAL_DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (!Thread.currentThread().isInterrupted()) {
            try {

                List<ServerNode> remotePods = serverNodeMapper.selectList(new LambdaQueryWrapper<ServerNode>()
                        .ge(ServerNode::getExpireAt, LocalDateTime.now())
                        .eq(ServerNode::getNodeType, NodeTypeEnum.SERVER.getType()));

                // 获取缓存中的节点
                ConcurrentMap<String/*hostId*/, RegisterNodeInfo> concurrentMap = CacheRegisterTable
                        .get(ServerRegister.GROUP_NAME);

                Set<String> remoteHostIds = remotePods.stream().map(ServerNode::getHostId).collect(Collectors.toSet());

                Set<String> localHostIds = concurrentMap.values().stream().map(RegisterNodeInfo::getHostId)
                        .collect(Collectors.toSet());

                List<GroupConfig> removeGroupConfig = configAccess.getAllOpenGroupConfig();
                Set<String> allGroup = CacheGroup.getAllGroup();

                // 无缓存的节点触发refreshCache
                if (CollectionUtils.isEmpty(concurrentMap)
                        // 节点数量不一致触发
                        || isNodeSizeNotEqual(remotePods.size(), concurrentMap.size())
                        // 若存在远程和本地缓存的组的数量不一致则触发rebalance
                        || isGroupSizeNotEqual(removeGroupConfig, allGroup)
                        // 判断远程节点是不是和本地节点一致的，如果不一致则重新分配
                        || isNodeNotMatch(remoteHostIds, localHostIds)) {

                    // 删除本地缓存以下线的节点信息
                    removeNode(concurrentMap, remoteHostIds, localHostIds);

                    // 刷新组配置和删除已关闭的组
                    refreshAndRemoveGroup(removeGroupConfig, allGroup);

                    // 重新获取DB中最新的服务信息
                    refreshCache(remotePods);

                    // 触发rebalance
                    doBalance();

                    // 每次rebalance之后给10秒作为空闲时间，等待其他的节点也完成rebalance
                    TimeUnit.SECONDS.sleep(INITIAL_DELAY);

                } else {

                    // 重新刷新所有的缓存key
                    refreshCache(remotePods);

                    // 再次获取最新的节点信息
                    concurrentMap = CacheRegisterTable
                            .get(ServerRegister.GROUP_NAME);

                    // 找出过期的节点
                    Set<RegisterNodeInfo> expireNodeSet = concurrentMap.values().stream()
                            .filter(registerNodeInfo -> registerNodeInfo.getExpireAt().isBefore(LocalDateTime.now()))
                            .collect(Collectors.toSet());
                    for (final RegisterNodeInfo registerNodeInfo : expireNodeSet) {
                        // 删除过期的节点信息
                        CacheRegisterTable.remove(registerNodeInfo.getGroupName(), registerNodeInfo.getHostId());
                        // 删除本地消费组信息
                        CacheConsumerGroup.remove(registerNodeInfo.getGroupName());
                    }

                }

            } catch (InterruptedException e) {
                LogUtils.error(log, "check balance interrupt");
            } catch (Exception e) {
                LogUtils.error(log, "check balance error", e);
            } finally {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private boolean isNodeNotMatch(Set<String> remoteHostIds, Set<String> localHostIds) {
        boolean b = !remoteHostIds.containsAll(localHostIds);
        if (b) {
            LogUtils.info(log, "判断远程节点是不是和本地节点一致. remoteHostIds:[{}] localHostIds:[{}]",
                    localHostIds,
                    remoteHostIds);
        }
        return b;
    }

    private boolean isNodeSizeNotEqual(int localNodeSize, int remoteNodeSize) {
        boolean b = localNodeSize != remoteNodeSize;
        if (b) {
            LogUtils.info(log, "存在远程和本地缓存的节点的数量不一致则触发rebalance. localNodeSize:[{}] remoteNodeSize:[{}]",
                    localNodeSize,
                    remoteNodeSize);
        }
        return b;
    }

    private boolean isGroupSizeNotEqual(List<GroupConfig> removeGroupConfig, Set<String> allGroup) {
        boolean b = allGroup.size() != removeGroupConfig.size();
        if (b) {
            LogUtils.info(log, "若存在远程和本地缓存的组的数量不一致则触发rebalance. localGroupSize:[{}] remoteGroupSize:[{}]",
                    allGroup.size(),
                    removeGroupConfig.size());
        }
        return b;
    }


}
