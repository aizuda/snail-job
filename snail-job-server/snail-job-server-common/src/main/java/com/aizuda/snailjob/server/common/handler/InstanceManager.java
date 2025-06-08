package com.aizuda.snailjob.server.common.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.ClientLoadBalance;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.allocate.client.ClientLoadBalanceManager;
import com.aizuda.snailjob.server.common.convert.RegisterNodeInfoConverter;
import com.aizuda.snailjob.server.common.dto.InstanceKey;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.dto.InstanceSelectCondition;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.register.ServerRegister;
import com.aizuda.snailjob.server.common.rpc.client.grpc.GrpcChannel;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Sets;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 实例管理器
 * </p>
 *
 * @author opensnail
 * @date 2025-05-24
 * @since 1.6.0-beta1
 */
@Component
@RequiredArgsConstructor
public class InstanceManager implements Lifecycle {
    private final ServerNodeMapper serverNodeMapper;
    private final Set<ConnectivityState> STATES = Sets.newHashSet(ConnectivityState.TRANSIENT_FAILURE, ConnectivityState.SHUTDOWN);
    private final int timeout = ServerRegister.DELAY_TIME + (ServerRegister.DELAY_TIME / 3);
    private static final ConcurrentHashMap<InstanceKey, InstanceLiveInfo> INSTANCE_MAP = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService INSTANCE_TIMEOUT_CHECK = Executors
            .newSingleThreadScheduledExecutor(r -> new Thread(r, "instance-timeout-check-thread"));

    /**
     * 注册或者更新实例信息
     *
     * @param info 注册的节点信息
     */
    public void registerOrUpdate(RegisterNodeInfo info) {
        InstanceKey instanceKey = InstanceKey.builder()
                .namespaceId(info.getNamespaceId())
                .groupName(info.getGroupName())
                .hostId(info.getHostId())
                .build();
        registerOrUpdate(instanceKey, info);
    }

    /**
     * 注册或者更新实例信息
     *
     * @param key  实例key
     * @param info 注册的节点信息
     */
    public void registerOrUpdate(InstanceKey key, RegisterNodeInfo info) {
        if (Objects.isNull(key) || Objects.isNull(info)) {
            SnailJobLog.LOCAL.error("Illegal registration of instance information");
            return;
        }

        INSTANCE_MAP.compute(key, (instanceKey, existing) -> {
            if (existing == null) {
                existing = new InstanceLiveInfo();
                existing.setNodeInfo(info);
                // 创建连接通道
                ManagedChannel channel = GrpcChannel.connect(info.getHostIp(), info.getHostPort());
                existing.setAlive(Objects.nonNull(channel));
                existing.setChannel(channel);
                existing.setLastUpdateAt(System.currentTimeMillis());
                return existing;
            } else {
                if (!existing.isAlive()) {
                    ConnectivityState channelState = existing.getChannel().getState(true);
                    if (STATES.contains(channelState)) {
                        // 连接已经失败，先置为false,也有可能重新连接上
                        SnailJobLog.LOCAL.warn("Node channel state check {}. {}", existing.getNodeInfo().address(), channelState);
                        // 直接返回等下下线即可
                        return existing;
                    }
                }
            }

            existing.setLastUpdateAt(System.currentTimeMillis());
            existing.setAlive(true);
            return existing;
        });
    }

    public InstanceLiveInfo getInstanceALiveInfoSet(InstanceKey instanceKey) {
        Set<InstanceLiveInfo> instanceALiveInfoSet = getInstanceALiveInfoSet(instanceKey.getNamespaceId(), instanceKey.getGroupName());
        return StreamUtils.filter(instanceALiveInfoSet, new Predicate<InstanceLiveInfo>() {
            @Override
            public boolean test(InstanceLiveInfo instanceLiveInfo) {
                return instanceLiveInfo.getNodeInfo().getHostId().equals(instanceKey.getHostId());
            }
        }).stream().findFirst().orElse(null);
    }

    public Set<InstanceLiveInfo> getInstanceALiveInfoSet(String namespaceId, String groupName, String targetLabels) {
        Map<String, String> targetLabelsMap;
        if (StrUtil.isNotBlank(targetLabels)) {
            targetLabelsMap = JsonUtil.parseHashMap(targetLabels);
        } else {
            targetLabelsMap = Collections.emptyMap();
        }

        // 默认匹配在线客户端实例（不匹配人为剥离流量的节点）
        targetLabelsMap.put(SystemConstants.DEFAULT_LABEL.getKey(), SystemConstants.DEFAULT_LABEL.getValue());
        Set<InstanceLiveInfo> instanceALiveInfoSet = getInstanceALiveInfoSet(namespaceId, groupName);
        Map<String, String> finalTargetLabelsMap = targetLabelsMap;
        return new HashSet<>(StreamUtils.filter(instanceALiveInfoSet, instanceLiveInfo -> {
            RegisterNodeInfo nodeInfo = instanceLiveInfo.getNodeInfo();
            return matchLabels(nodeInfo.getLabelMap(), finalTargetLabelsMap);
        }));
    }


    public Set<InstanceLiveInfo> getInstanceALiveInfoSet(String namespaceId, String groupName,  Map<String, String> targetLabels) {
        Set<InstanceLiveInfo> instanceALiveInfoSet = getInstanceALiveInfoSet(namespaceId, groupName);
        return new HashSet<>(StreamUtils.filter(instanceALiveInfoSet, instanceLiveInfo -> {
            RegisterNodeInfo nodeInfo = instanceLiveInfo.getNodeInfo();
            return matchLabels(nodeInfo.getLabelMap(), targetLabels);
        }));
    }

    /**
     * 获取存活的实例信息
     *
     * @param namespaceId 空间id
     * @param groupName   组信息
     * @return Set<InstanceLiveInfo>
     */
    public Set<InstanceLiveInfo> getInstanceALiveInfoSet(String namespaceId, String groupName) {
        Set<InstanceLiveInfo> allPods = getAllInstances().stream()
                .filter(instanceLiveInfo -> {
                    RegisterNodeInfo nodeInfo = instanceLiveInfo.getNodeInfo();
                    return nodeInfo.getGroupName().equals(groupName)
                            && nodeInfo.getNamespaceId().equals(namespaceId);
                }).collect(Collectors.toSet());
        if (CollUtil.isEmpty(allPods)) {
            // 此处为了降级，若缓存中没有则取DB中查询
            List<ServerNode> serverNodes = serverNodeMapper.selectList(
                    new LambdaQueryWrapper<ServerNode>()
                            .eq(ServerNode::getNamespaceId, namespaceId)
                            .eq(ServerNode::getGroupName, groupName));
            if (CollUtil.isEmpty(serverNodes)) {
                return Sets.newHashSet();
            }

            for (final ServerNode node : serverNodes) {
                // 刷新全量本地缓存
                registerOrUpdate(RegisterNodeInfoConverter.INSTANCE.toRegisterNodeInfo(node));
            }

            return getInstanceALiveInfoSet(namespaceId, groupName);
        }

        return allPods.stream().filter(InstanceLiveInfo::isAlive).collect(Collectors.toSet());
    }

    /**
     * 获取所有实例包括不存活的
     *
     * @return 缓存对象
     */
    public Set<InstanceLiveInfo> getAllInstances() {
        Collection<InstanceLiveInfo> values = INSTANCE_MAP.values();
        return new TreeSet<>(values);
    }

    /**
     * 根据路由策略获取实例节点
     *
     * @param conditionDTO 筛选条件
     */
    public InstanceLiveInfo getALiveInstanceByRouteKey(InstanceSelectCondition conditionDTO) {

        Set<InstanceLiveInfo> instanceLiveInfos = getInstanceALiveInfoSet(conditionDTO.getNamespaceId(), conditionDTO.getGroupName());
        if (CollUtil.isEmpty(instanceLiveInfos)) {
            SnailJobLog.LOCAL.warn("client node is null. groupName:[{}]", conditionDTO.getGroupName());
            return null;
        }

        Set<RegisterNodeInfo> registerNodeInfos = instanceLiveInfos.stream()
                .map(InstanceLiveInfo::getNodeInfo)
                .filter(node -> matchLabels(node.getLabelMap(), conditionDTO.getTargetLabels()))
                .collect(Collectors.toSet());

        ClientLoadBalance clientLoadBalanceRandom = ClientLoadBalanceManager.getClientLoadBalance(conditionDTO.getRouteKey());

        String hostId = clientLoadBalanceRandom.route(conditionDTO.getAllocKey(), new TreeSet<>(StreamUtils.toSet(registerNodeInfos, RegisterNodeInfo::getHostId)));

        Stream<InstanceLiveInfo> registerNodeInfoStream = instanceLiveInfos.stream()
                .filter(s -> s.getNodeInfo().getHostId().equals(hostId));
        return registerNodeInfoStream.findFirst().orElse(null);
    }

    /**
     * 获取实例信息(包含不存活的节点)
     *
     * @param key 实例key
     * @return
     */
    public InstanceLiveInfo getInstance(InstanceKey key) {
        InstanceLiveInfo instanceLiveInfo = INSTANCE_MAP.get(key);
        if (Objects.isNull(instanceLiveInfo)) {
            // 此处为了降级，若缓存中没有则取DB中查询
            List<ServerNode> serverNodes = serverNodeMapper.selectList(
                    new LambdaQueryWrapper<ServerNode>()
                            .eq(ServerNode::getNamespaceId, key.getNamespaceId())
                            .eq(ServerNode::getGroupName, key.getGroupName())
                            .eq(ServerNode::getHostId, key.getHostId())
                            .orderByDesc(ServerNode::getExpireAt));
            if (CollUtil.isEmpty(serverNodes)) {
                return null;
            }

            registerOrUpdate(RegisterNodeInfoConverter.INSTANCE.toRegisterNodeInfo(serverNodes.get(0)));
            return INSTANCE_MAP.get(key);
        }

        return instanceLiveInfo;
    }

    public void remove(InstanceKey key) {
        INSTANCE_MAP.remove(key);
    }

    @Override
    public void start() {
        INSTANCE_TIMEOUT_CHECK.scheduleAtFixedRate(() -> {

            try {
                // 剔除超时的执行器
                long now = DateUtils.toNowMilli();
                for (Map.Entry<InstanceKey, InstanceLiveInfo> entry : INSTANCE_MAP.entrySet()) {
                    InstanceLiveInfo info = entry.getValue();
                    ManagedChannel channel = info.getChannel();
                    ConnectivityState channelState = channel.getState(!info.isAlive());
                    if (STATES.contains(channelState)) {
                        // 连接已经失败，先置为false,也有可能重新连接上
                        SnailJobLog.LOCAL.warn("Node channel state check {}. {}", info.getNodeInfo().address(), channelState);
                        info.setAlive(Boolean.FALSE);
                    } else {
                        info.setAlive(Boolean.TRUE);
                    }

                    if (now - info.getLastUpdateAt() > TimeUnit.SECONDS.toMillis(timeout)
                            || channel.isShutdown()
                            || channel.isTerminated()) {
                        SnailJobLog.LOCAL.info("Node {} is offline. Removing...", info.getNodeInfo().address());
                        INSTANCE_MAP.remove(entry.getKey());
                        channel.shutdown();
                    }
                }
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("instance timeout check is error", e);
            }

        }, 0, SystemConstants.SCHEDULE_PERIOD, TimeUnit.SECONDS);
    }

    @Override
    public void close() {

    }

    /**
     * 匹配到了一个就可以
     * 节点注册时的标签必须是全包含任务的标签
     * <p>
     * [] [] true
     * [] [A] true
     * [A] [] false
     * <p>
     * [A,B] [A] true
     * [A,B] [A, B] true
     * <p>
     * [A,B] [A,C] false
     * [A,B] [E] false
     *
     * @param nodeLabels   节点注册时的标签
     * @param targetLabels 任务的标签
     * @return
     */
    private boolean matchLabels(Map<String, String> nodeLabels, Map<String, String> targetLabels) {
        // 兼容客户端无标签的问题
        if (CollUtil.isEmpty(nodeLabels)) {
            return true;
        }

        for (Map.Entry<String, String> entry : targetLabels.entrySet()) {
            if (!entry.getValue().equals(nodeLabels.get(entry.getKey()))) {
                return false;
            }
        }

        return true;
    }

}
