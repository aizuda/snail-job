package com.aizuda.easy.retry.server.common.cache;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.NodeTypeEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.RegisterNodeInfoConverter;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.register.ServerRegister;
import com.aizuda.easy.retry.server.common.triple.Pair;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.ServerNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.ServerNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * POD注册表
 *
 * @author www.byteblogs.com
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
        if (CollectionUtils.isEmpty(concurrentMap)) {
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
            if (CollectionUtils.isEmpty(serverNodes)) {
                return null;
            }

            CacheRegisterTable.addOrUpdate(serverNodes.get(0));

            concurrentMap = CACHE.getIfPresent(getKey(groupName, namespaceId));
            if (CollectionUtils.isEmpty(concurrentMap)) {
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
        if (CollectionUtils.isEmpty(concurrentMap)) {

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
            if (CollectionUtils.isEmpty(serverNodes) || CollectionUtils.isEmpty(concurrentMap)) {
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
        return getServerNodeSet(groupName, namespaceId).stream()
            .map(RegisterNodeInfo::getHostId).collect(Collectors.toSet());
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
            EasyRetryLog.LOCAL.warn("node not exists. groupName:[{}] hostId:[{}]", serverNode.getGroupName(),
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
            EasyRetryLog.LOCAL.info("Add cache. groupName:[{}] namespaceId:[{}] hostId:[{}]", serverNode.getGroupName(),
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

        EasyRetryLog.LOCAL.info("Remove cache. groupName:[{}] hostId:[{}]", groupName, hostId);
        concurrentMap.remove(hostId);
    }

    @Override
    public void start() {
        EasyRetryLog.LOCAL.info("CacheRegisterTable start");
        CACHE = CacheBuilder.newBuilder()
            // 设置并发级别为cpu核心数
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            // 设置写缓存后60秒过期
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build();
    }


    @Override
    public void close() {
        EasyRetryLog.LOCAL.info("CacheRegisterTable stop");
        CACHE.invalidateAll();
    }
}
