package com.aizuda.snailjob.server.common.cache;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.context.SpringContext;
import com.aizuda.snailjob.common.core.enums.NodeTypeEnum;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.RegisterNodeInfoConverter;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.register.ServerRegister;
import com.aizuda.snailjob.server.common.triple.Pair;
import com.aizuda.snailjob.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * POD注册表
 *
 * @author opensnail
 * @date 2021-10-30
 * @since 1.6.0
 */
@Component
@Slf4j
public class CacheRegisterTable implements Lifecycle {

    private static Cache<Pair<String/*groupName*/, String/*namespaceId*/>, ConcurrentMap<String, RegisterNodeInfo>> CACHE;

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static Set<RegisterNodeInfo> getAllPods() {
        ConcurrentMap<Pair<String, String>, ConcurrentMap<String, RegisterNodeInfo>> concurrentMap = CACHE.asMap();
        if (CollUtil.isEmpty(concurrentMap)) {
            return Sets.newHashSet();
        }

        return concurrentMap.values().stream()
                .map(stringServerNodeConcurrentMap -> new TreeSet(stringServerNodeConcurrentMap.values()))
                .reduce((s, y) -> {
                    s.addAll(y);
                    return s;
                }).orElse(new TreeSet<>());

    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static ConcurrentMap<String, RegisterNodeInfo> get(String groupName, String namespaceId) {
        return CACHE.getIfPresent(getKey(groupName, namespaceId));
    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static RegisterNodeInfo getServerNode(String groupName, String namespaceId, String hostId) {
        ConcurrentMap<String, RegisterNodeInfo> concurrentMap = CACHE.getIfPresent(getKey(groupName, namespaceId));
        if (Objects.isNull(concurrentMap)) {
            // 此处为了降级，若缓存中没有则取DB中查询
            ServerNodeMapper serverNodeMapper = SpringContext.getBeanByType(ServerNodeMapper.class);
            List<ServerNode> serverNodes = serverNodeMapper.selectList(
                    new LambdaQueryWrapper<ServerNode>()
                            .eq(ServerNode::getNodeType, NodeTypeEnum.CLIENT.getType())
                            .eq(ServerNode::getNamespaceId, namespaceId)
                            .eq(ServerNode::getGroupName, groupName)
                            .eq(ServerNode::getHostId, hostId)
                            .orderByDesc(ServerNode::getExpireAt));
            if (CollUtil.isEmpty(serverNodes)) {
                return null;
            }

            CacheRegisterTable.addOrUpdate(serverNodes.get(0));

            concurrentMap = CACHE.getIfPresent(getKey(groupName, namespaceId));
            if (CollUtil.isEmpty(concurrentMap)) {
                return null;
            }
        }

        return concurrentMap.get(hostId);
    }

    /**
     * 获取排序的ServerNode
     *
     * @return 缓存对象
     */
    public static Set<RegisterNodeInfo> getServerNodeSet(String groupName, String namespaceId) {
        ConcurrentMap<String, RegisterNodeInfo> concurrentMap = CACHE.getIfPresent(getKey(groupName, namespaceId));
        if (CollUtil.isEmpty(concurrentMap)) {

            // 此处为了降级，若缓存中没有则取DB中查询
            ServerNodeMapper serverNodeMapper = SpringContext.getBeanByType(ServerNodeMapper.class);
            List<ServerNode> serverNodes = serverNodeMapper.selectList(
                    new LambdaQueryWrapper<ServerNode>()
                            .eq(ServerNode::getNodeType, NodeTypeEnum.CLIENT.getType())
                            .eq(ServerNode::getNamespaceId, namespaceId)
                            .eq(ServerNode::getGroupName, groupName));
            for (final ServerNode node : serverNodes) {
                // 刷新全量本地缓存
                CacheRegisterTable.addOrUpdate(node);
            }

            concurrentMap = CACHE.getIfPresent(getKey(groupName, namespaceId));
            if (CollUtil.isEmpty(serverNodes) || CollUtil.isEmpty(concurrentMap)) {
                return Sets.newHashSet();
            }
        }

        return new TreeSet<>(concurrentMap.values());
    }

    private static Pair<String, String> getKey(final String groupName, final String namespaceId) {
        return Pair.of(groupName, namespaceId);
    }

    /**
     * 获取排序的hostId
     *
     * @return 缓存对象
     */
    public static Set<String> getPodIdSet(String groupName, String namespaceId) {
        return StreamUtils.toSet(getServerNodeSet(groupName, namespaceId), RegisterNodeInfo::getHostId);
    }


    /**
     * 刷新过期时间若不存在则初始化
     *
     * @param serverNode 服务节点
     */
    public static synchronized void refreshExpireAt(ServerNode serverNode) {
        RegisterNodeInfo registerNodeInfo = getServerNode(serverNode.getGroupName(), serverNode.getNamespaceId(),
                serverNode.getHostId());
        // 不存在则初始化
        if (Objects.isNull(registerNodeInfo)) {
            SnailJobLog.LOCAL.warn("node not exists. groupName:[{}] hostId:[{}]", serverNode.getGroupName(),
                    serverNode.getHostId());
        } else {
            // 存在则刷新过期时间
            registerNodeInfo.setExpireAt(serverNode.getExpireAt());
        }
    }

    /**
     * 无缓存时添加 有缓存时更新
     *
     * @return 缓存对象
     */
    public static synchronized void addOrUpdate(ServerNode serverNode) {
        ConcurrentMap<String, RegisterNodeInfo> concurrentMap = CACHE.getIfPresent(
                getKey(serverNode.getGroupName(), serverNode.getNamespaceId()));
        RegisterNodeInfo registerNodeInfo;
        if (Objects.isNull(concurrentMap)) {
            SnailJobLog.LOCAL.info("Add cache. groupName:[{}] namespaceId:[{}] hostId:[{}]", serverNode.getGroupName(),
                    serverNode.getNamespaceId(), serverNode.getHostId());
            concurrentMap = new ConcurrentHashMap<>();
            registerNodeInfo = RegisterNodeInfoConverter.INSTANCE.toRegisterNodeInfo(serverNode);

        } else {
            // 复用缓存中的对象
            registerNodeInfo = concurrentMap.getOrDefault(serverNode.getHostId(),
                    RegisterNodeInfoConverter.INSTANCE.toRegisterNodeInfo(serverNode));
            registerNodeInfo.setExpireAt(serverNode.getExpireAt());

            // 删除过期的节点信息
            delExpireNode(concurrentMap);
        }

        concurrentMap.put(serverNode.getHostId(), registerNodeInfo);
        // 此缓存设置了60秒没有写入即过期，因此这次刷新缓存防止过期
        CACHE.put(getKey(serverNode.getGroupName(), serverNode.getNamespaceId()), concurrentMap);
    }

    /**
     * 删除过期的节点信息
     *
     * @param concurrentMap 并发映射的节点信息
     */
    private static void delExpireNode(final ConcurrentMap<String, RegisterNodeInfo> concurrentMap) {
        concurrentMap.values().stream()
                .filter(registerNodeInfo -> registerNodeInfo.getExpireAt().isBefore(
                        LocalDateTime.now().minusSeconds(ServerRegister.DELAY_TIME + (ServerRegister.DELAY_TIME / 3))))
                .forEach(registerNodeInfo -> remove(registerNodeInfo.getGroupName(),
                        registerNodeInfo.getNamespaceId(), registerNodeInfo.getHostId()));
    }


    /**
     * 删除缓存
     *
     * @param groupName 组名称
     * @param hostId    机器id
     */
    public static void remove(String groupName, String namespaceId, String hostId) {
        ConcurrentMap<String, RegisterNodeInfo> concurrentMap = CACHE.getIfPresent(getKey(groupName, namespaceId));
        if (Objects.isNull(concurrentMap)) {
            return;
        }

        SnailJobLog.LOCAL.info("Remove cache. groupName:[{}] hostId:[{}]", groupName, hostId);
        concurrentMap.remove(hostId);
    }

    @Override
    public void start() {
        SnailJobLog.LOCAL.info("CacheRegisterTable start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                // 设置写缓存后60秒过期
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }


    @Override
    public void close() {
        SnailJobLog.LOCAL.info("CacheRegisterTable stop");
        CACHE.invalidateAll();
    }
}
