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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author: shuguang.zhang
 * @date : 2023-06-08 15:58
 */
@Component
@Slf4j
public class ServerNodeBalance implements Lifecycle {

    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;
    private final ScheduledExecutorService serverRegisterNode = Executors
        .newSingleThreadScheduledExecutor(r -> new Thread(r, "server-node-balance"));

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
            // 已经按照id 正序排序
            List<GroupConfig> prepareAllocateGroupConfig = configAccess.getAllOpenGroupConfig();
            if (CollectionUtils.isEmpty(prepareAllocateGroupConfig)) {
                return;
            }

            // 为了保证客户端分配算法的一致性,serverNodes 从数据库从数据获取
            Set<String> podIpSet = CacheRegisterTable.getPodIdSet(ServerRegister.GROUP_NAME);

            if (CollectionUtils.isEmpty(podIpSet)) {
                LogUtils.error(log, "服务端节点为空");
                return;
            }

            Set<String> groupNameSet = prepareAllocateGroupConfig.stream().map(GroupConfig::getGroupName)
                .collect(Collectors.toSet());

            List<String> allocate = new AllocateMessageQueueConsistentHash()
                .allocate(ServerRegister.CURRENT_CID, new ArrayList<>(groupNameSet), new ArrayList<>(podIpSet));
            CacheConsumerGroup.clear();
            for (final String groupName : allocate) {
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
        serverRegisterNode.scheduleAtFixedRate(() -> {
            try {

                List<ServerNode> remotePods = serverNodeMapper.selectList(new LambdaQueryWrapper<ServerNode>()
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
                    || remotePods.size() != concurrentMap.size()
                    // 若存在新的组则触发rebalance
                    || allGroup.size() != removeGroupConfig.size()
                    // 判断远程节点是不是和本地节点一致的，如果不一致则重新分配
                    || !remoteHostIds.containsAll(localHostIds)) {

                    localHostIds.removeAll(remoteHostIds);
                    for (String localHostId : localHostIds) {
                        RegisterNodeInfo registerNodeInfo = concurrentMap.get(localHostId);
                        // 删除过期的节点信息
                        CacheRegisterTable.remove(registerNodeInfo.getGroupName(), registerNodeInfo.getHostId());
                        // 删除本地消费组信息
                        CacheConsumerGroup.remove(registerNodeInfo.getGroupName());
                    }

                    // 刷新组配置和删除已关闭的组
                    refreshAndRemoveGroup(removeGroupConfig, allGroup);

                    // 重新获取DB中最新的服务信息
                    refreshCache(remotePods);

                    // 触发rebalance
                    doBalance();


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

            } catch (Exception e) {
                LogUtils.error(log, "check balance error", e);
            }
        }, 10, 1, TimeUnit.SECONDS);
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
        serverRegisterNode.shutdown();

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
}
