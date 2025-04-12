package com.aizuda.snailjob.server.common.handler;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.enums.NodeTypeEnum;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.allocate.server.AllocateMessageQueueAveragely;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.DistributeInstance;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.register.ServerRegister;
import com.aizuda.snailjob.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 负责处理组或者节点变化时，重新分配组在不同的节点上消费
 *
 * @author: opensnail
 * @date : 2023-06-08 15:58
 * @since 1.6.0
 */
@Component
@RequiredArgsConstructor
public class ServerNodeBalance implements Lifecycle, Runnable {

    /**
     * 延迟10s为了尽可能保障集群节点都启动完成在进行rebalance
     */
    public static final Long INITIAL_DELAY = 10L;
    private final ServerNodeMapper serverNodeMapper;
    private final SystemProperties systemProperties;

    private Thread thread = null;
    private List<Integer> bucketList;

    public void doBalance() {
        SnailJobLog.LOCAL.info("rebalance start");
        DistributeInstance.RE_BALANCE_ING.set(Boolean.TRUE);

        try {

            // 为了保证客户端分配算法的一致性,serverNodes 从数据库从数据获取
            Set<String> podIpSet = CacheRegisterTable.getPodIdSet(ServerRegister.GROUP_NAME, ServerRegister.NAMESPACE_ID);

            if (CollUtil.isEmpty(podIpSet)) {
                SnailJobLog.LOCAL.error("server node is empty");
            }

            // 删除本地缓存的消费桶的信息
            DistributeInstance.INSTANCE.clearConsumerBucket();
            if (CollUtil.isEmpty(podIpSet)) {
                return;
            }

            List<Integer> allocate = new AllocateMessageQueueAveragely()
                    .allocate(ServerRegister.CURRENT_CID, bucketList, new ArrayList<>(podIpSet));

            // 重新覆盖本地分配的bucket
            DistributeInstance.INSTANCE.setConsumerBucket(allocate);

            SnailJobLog.LOCAL.info("rebalance complete. allocate:[{}]", allocate);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("rebalance error. ", e);
        } finally {
            DistributeInstance.RE_BALANCE_ING.set(Boolean.FALSE);
        }

    }

    @Override
    public void start() {

        int bucketTotal = systemProperties.getBucketTotal();
        bucketList = new ArrayList<>(bucketTotal);
        for (int i = 0; i < bucketTotal; i++) {
            bucketList.add(i);
        }

        SnailJobLog.LOCAL.info("ServerNodeBalance start");
        thread = new Thread(this, "server-node-balance");
        thread.start();
    }

    private void removeNode(ConcurrentMap<String, RegisterNodeInfo> concurrentMap, Set<String> remoteHostIds, Set<String> localHostIds) {

        localHostIds.removeAll(remoteHostIds);
        for (String localHostId : localHostIds) {
            RegisterNodeInfo registerNodeInfo = concurrentMap.get(localHostId);
            // 删除过期的节点信息
            CacheRegisterTable.remove(registerNodeInfo.getGroupName(), registerNodeInfo.getNamespaceId(), registerNodeInfo.getHostId());
        }
    }

    private void refreshExpireAtCache(List<ServerNode> remotePods) {
        // 重新刷新缓存
        refreshCache(remotePods);
    }

    private void refreshCache(List<ServerNode> remotePods) {

        // 刷新最新的节点注册信息
        for (ServerNode node : remotePods) {
            CacheRegisterTable.addOrUpdate(node);
        }
    }

    @Override
    public void close() {

        // 停止定时任务
        thread.interrupt();

        SnailJobLog.LOCAL.info("ServerNodeBalance start. ");
        int i = serverNodeMapper
                .delete(new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getHostId, ServerRegister.CURRENT_CID));
        if (1 == i) {
            SnailJobLog.LOCAL.info("delete node success. [{}]", ServerRegister.CURRENT_CID);
        } else {
            SnailJobLog.LOCAL.info("delete node  error. [{}]", ServerRegister.CURRENT_CID);
        }

        SnailJobLog.LOCAL.info("ServerNodeBalance close complete");
    }

    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(INITIAL_DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        while (!Thread.currentThread().isInterrupted()) {
            try {

                List<ServerNode> remotePods = serverNodeMapper.selectList(new LambdaQueryWrapper<ServerNode>()
                        .ge(ServerNode::getExpireAt, LocalDateTime.now())
                        .eq(ServerNode::getNodeType, NodeTypeEnum.SERVER.getType()));

                // 获取缓存中的节点
                ConcurrentMap<String/*hostId*/, RegisterNodeInfo> concurrentMap = Optional.ofNullable(CacheRegisterTable
                        .get(ServerRegister.GROUP_NAME, ServerRegister.NAMESPACE_ID)).orElse(new ConcurrentHashMap<>());

                Set<String> remoteHostIds = StreamUtils.toSet(remotePods, ServerNode::getHostId);

                Set<String> localHostIds = StreamUtils.toSet(concurrentMap.values(), RegisterNodeInfo::getHostId);

                // 无缓存的节点触发refreshCache
                if (CollUtil.isEmpty(concurrentMap)
                        // 节点数量不一致触发
                        || isNodeSizeNotEqual(concurrentMap.size(), remotePods.size())
                        // 判断远程节点是不是和本地节点一致的，如果不一致则重新分配
                        || isNodeNotMatch(remoteHostIds, localHostIds)
                        // 检查当前节点的消费桶是否为空，为空则重新负载
                        || checkConsumerBucket(remoteHostIds)
                ) {

                    // 删除本地缓存以下线的节点信息
                    removeNode(concurrentMap, remoteHostIds, localHostIds);

                    // 重新获取DB中最新的服务信息
                    refreshCache(remotePods);

                    // 触发rebalance
                    doBalance();

                    // 每次rebalance之后给10秒作为空闲时间，等待其他的节点也完成rebalance
                    TimeUnit.SECONDS.sleep(INITIAL_DELAY);

                } else {

                    // 刷新过期时间
                    refreshExpireAtCache(remotePods);

                    // 再次获取最新的节点信息
                    concurrentMap = CacheRegisterTable
                            .get(ServerRegister.GROUP_NAME, ServerRegister.NAMESPACE_ID);

                    // 找出过期的节点
                    Set<RegisterNodeInfo> expireNodeSet = concurrentMap.values().stream()
                            .filter(registerNodeInfo -> registerNodeInfo.getExpireAt().isBefore(LocalDateTime.now()))
                            .collect(Collectors.toSet());
                    for (final RegisterNodeInfo registerNodeInfo : expireNodeSet) {
                        // 删除过期的节点信息
                        CacheRegisterTable.remove(registerNodeInfo.getGroupName(), registerNodeInfo.getNamespaceId(), registerNodeInfo.getHostId());
                    }

                }

            } catch (InterruptedException e) {
                SnailJobLog.LOCAL.info("check balance stop");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("check balance error", e);
            } finally {
                try {
                    TimeUnit.SECONDS.sleep(systemProperties.getLoadBalanceCycleTime());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

    }

    private boolean isNodeNotMatch(Set<String> remoteHostIds, Set<String> localHostIds) {
        boolean b = !remoteHostIds.containsAll(localHostIds);
        if (b) {
            SnailJobLog.LOCAL.info("Determine if remote nodes match local nodes. Remote host IDs:[{}] Local host IDs:[{}]",
                    localHostIds,
                    remoteHostIds);
        }

        // 若在线节点小于总的Bucket数量且当前节点无任何分桶，则需要重新负载
        if (CollUtil.isEmpty(DistributeInstance.INSTANCE.getConsumerBucket()) && remoteHostIds.size() <= systemProperties.getBucketTotal()) {
            return true;
        }

        return b;
    }

    public boolean checkConsumerBucket(Set<String> remoteHostIds) {
        return CollUtil.isEmpty(DistributeInstance.INSTANCE.getConsumerBucket()) && remoteHostIds.size() <= systemProperties.getBucketTotal();
    }

    private boolean isNodeSizeNotEqual(int localNodeSize, int remoteNodeSize) {
        boolean b = localNodeSize != remoteNodeSize;
        if (b) {
            SnailJobLog.LOCAL.info("If the number of nodes cached remotely and locally is inconsistent, trigger rebalance. Local node size:[{}] Remote node size:[{}]",
                    localNodeSize,
                    remoteNodeSize);
        }
        return b;
    }

}
