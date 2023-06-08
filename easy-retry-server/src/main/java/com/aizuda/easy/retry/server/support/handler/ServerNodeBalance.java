package com.aizuda.easy.retry.server.support.handler;

import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.server.support.Lifecycle;
import com.aizuda.easy.retry.server.support.allocate.server.AllocateMessageQueueConsistentHash;
import com.aizuda.easy.retry.server.support.cache.CacheConsumerGroup;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final ScheduledExecutorService serverRegisterNode = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "ServerRegisterNode"));

    @Autowired
    protected ServerNodeMapper serverNodeMapper;

    public void doBalance() {

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
        for (final String groupName : allocate) {
            CacheConsumerGroup.addOrUpdate(groupName);

        }
    }

    @Override
    public void start() {
        LogUtils.info(log, "ServerNodeBalance start");
        serverRegisterNode.scheduleAtFixedRate(() -> {
            try {
                ConcurrentMap<String, ServerNode> concurrentMap = CacheRegisterTable.get(ServerRegister.GROUP_NAME);

                if (!CollectionUtils.isEmpty(concurrentMap)) {
                    Set<ServerNode> sorted = new TreeSet<>(Comparator.comparing(ServerNode::getExpireAt));
                    sorted.addAll(concurrentMap.values());

                    sorted.stream().findFirst().ifPresent(serverNode -> {
                        // 若服务端的POD注册的第一个节点是过期了则触发rebalance
                        if (serverNode.getExpireAt().isBefore(LocalDateTime.now())) {
                            // 删除过期的节点信息
                            CacheRegisterTable.remove(serverNode.getGroupName(), serverNode.getHostId());
                            // 删除本地消费组信息
                            CacheConsumerGroup.remove(serverNode.getGroupName());
                            // 重新刷新所有的缓存key
                            refreshCache();
                            // 触发rebalance
                            doBalance();
                        }
                    });
                } else {
                    // 若不存在服务端的POD注册信息直接rebalance
                    // 重新获取DB中最新的服务信息
                    refreshCache();

                    // 触发rebalance
                    doBalance();
                }

            } catch (Exception e) {
                LogUtils.error(log, "", e);
            }
        }, 10, 1, TimeUnit.SECONDS);
    }

    private void refreshCache() {
        // 重新获取DB中最新的服务信息
        List<ServerNode> serverNodes = serverNodeMapper.selectList(
                new LambdaQueryWrapper<ServerNode>()
                        .eq(ServerNode::getNodeType, NodeTypeEnum.SERVER.getType()));

        // 刷新最新的节点注册信息
        for (ServerNode node : serverNodes) {
            CacheRegisterTable.addOrUpdate(node.getGroupName(), node);
        }
    }

    @Override
    public void close() {
        LogUtils.info(log, "ServerNodeBalance close");
        LogUtils.info(log, "准备删除节点 [{}]", ServerRegister.CURRENT_CID);
        int i = serverNodeMapper.delete(new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getHostId, ServerRegister.CURRENT_CID));
        if (1 == i) {
            LogUtils.info(log,"删除节点 [{}]成功", ServerRegister.CURRENT_CID);
        } else {
            LogUtils.info(log,"删除节点 [{}]失败", ServerRegister.CURRENT_CID);
        }
    }
}
