package com.aizuda.snailjob.server.common.handler;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.enums.NodeTypeEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.allocate.server.AllocateMessageQueueAveragely;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.convert.RegisterNodeInfoConverter;
import com.aizuda.snailjob.server.common.dto.DistributeInstance;
import com.aizuda.snailjob.server.common.dto.InstanceKey;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.register.ServerRegister;
import com.aizuda.snailjob.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private final InstanceManager instanceManager;

    /**
     * 延迟10s为了尽可能保障集群节点都启动完成在进行rebalance
     */
    public static final Long INITIAL_DELAY = 10L;
    private final ServerNodeMapper serverNodeMapper;
    private final SystemProperties systemProperties;

    private Thread thread = null;
    private List<Integer> bucketList;

    public void doBalance(Set<String> remoteHostIds) {
        SnailJobLog.LOCAL.info("rebalance start remoteHostIds:{}", JsonUtil.toJsonString(remoteHostIds));
        DistributeInstance.RE_BALANCE_ING.set(Boolean.TRUE);

        try {
            if (CollUtil.isEmpty(remoteHostIds)) {
                SnailJobLog.LOCAL.error("server node is empty");
            }

            // 删除本地缓存的消费桶的信息
            DistributeInstance.INSTANCE.clearConsumerBucket();
            if (CollUtil.isEmpty(remoteHostIds)) {
                return;
            }

            List<Integer> allocate = new AllocateMessageQueueAveragely()
                    .allocate(ServerRegister.CURRENT_CID, bucketList, new ArrayList<>(remoteHostIds));

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

    private void removeNode(Set<String> remoteHostIds, Set<String> localHostIds) {

        localHostIds.removeAll(remoteHostIds);
        for (String localHostId : localHostIds) {
            InstanceKey instanceKey = InstanceKey.builder()
                    .namespaceId(ServerRegister.NAMESPACE_ID)
                    .groupName(ServerRegister.GROUP_NAME)
                    .hostId(localHostId)
                    .build();
            // 删除过期的节点信息
            instanceManager.remove(instanceKey);
        }
    }

    private void refreshExpireAtCache(List<ServerNode> remotePods) {
        // 重新刷新缓存
        refreshCache(remotePods);
    }

    private void refreshCache(List<ServerNode> remotePods) {

        // 刷新最新的节点注册信息
        for (ServerNode node : remotePods) {
            instanceManager.registerOrUpdate(RegisterNodeInfoConverter.INSTANCE.toRegisterNodeInfo(node));
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
                Set<String> localHostIds = instanceManager
                        .getAllCacheInstanceHostIdSet(ServerRegister.NAMESPACE_ID, ServerRegister.GROUP_NAME);

                Set<String> remoteHostIds = new TreeSet<>(StreamUtils.toSet(remotePods, ServerNode::getHostId));

                // 无缓存的节点触发refreshCache
                if (CollUtil.isEmpty(localHostIds)
                        // 节点数量不一致触发
                        || isNodeSizeNotEqual(localHostIds.size(), remotePods.size())
                        // 判断远程节点是不是和本地节点一致的，如果不一致则重新分配
                        || isNodeNotMatch(remoteHostIds, localHostIds)
                        // 检查当前节点的消费桶是否为空，为空则重新负载
                        || checkConsumerBucket(remoteHostIds)
                ) {

                    SnailJobLog.LOCAL.info(
                            "Node change detected, starting load balancing. localHostIds:{} remoteHostIds:{}",
                            JsonUtil.toJson(localHostIds),
                            JsonUtil.toJson(remoteHostIds)
                    );

                    // 删除本地缓存以下线的节点信息
                    removeNode(remoteHostIds, localHostIds);

                    // 重新获取DB中最新的服务信息
                    refreshCache(remotePods);

                    // 触发rebalance
                    doBalance(remoteHostIds);

                    // 每次rebalance之后给10秒作为空闲时间，等待其他的节点也完成rebalance
                    TimeUnit.SECONDS.sleep(INITIAL_DELAY);

                } else {

                    // 刷新节点过期时间
                    refreshExpireAtCache(remotePods);
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
