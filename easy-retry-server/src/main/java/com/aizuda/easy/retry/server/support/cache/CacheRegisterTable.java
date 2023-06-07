package com.aizuda.easy.retry.server.support.cache;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.persistence.mybatis.po.ServerNode;
import com.aizuda.easy.retry.server.support.Lifecycle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Objects;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * POD注册表
 *
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 2.0
 */
@Component
@Data
@Slf4j
public class CacheRegisterTable implements Lifecycle {

    private static Cache<String, ConcurrentMap<String, ServerNode>> CACHE;

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static ConcurrentMap<String, ServerNode> get(String groupName) {
        return CACHE.getIfPresent(groupName);
    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static ServerNode getServerNode(String groupName, String hostId) {
        ConcurrentMap<String, ServerNode> concurrentMap = CACHE.getIfPresent(groupName);
        if (Objects.isNull(concurrentMap)) {
            return null;
        }

        return concurrentMap.get(hostId);
    }

    /**
     * 获取排序的ServerNode
     *
     * @return 缓存对象
     */
    public static TreeSet<ServerNode> getServerNodeSet(String groupName) {
        ConcurrentMap<String, ServerNode> concurrentMap = CACHE.getIfPresent(groupName);
        if (Objects.isNull(concurrentMap)) {
            return new TreeSet<>();
        }

        return new TreeSet<>(Comparator.comparingInt(o -> o.getId().intValue()));
    }

    /**
     * 获取排序的ServerNode
     *
     * @return 缓存对象
     */
    public static Set<String> gePodIpSet(String groupName) {
        return getServerNodeSet(groupName).stream().map(ServerNode::getHostIp).collect(Collectors.toSet());
    }

    /**
     * 获取所有缓存
     *
     * @return 缓存对象
     */
    public static void put(String groupName, ServerNode serverNode) {
        ConcurrentMap<String, ServerNode> concurrentMap = CACHE.getIfPresent(groupName);
        if (Objects.isNull(concurrentMap)) {
            concurrentMap = new ConcurrentHashMap<>();
        }
        concurrentMap.put(serverNode.getHostId(), serverNode);
    }

    public static void remove(String groupName, String hostId) {
        ConcurrentMap<String, ServerNode> concurrentMap = CACHE.getIfPresent(groupName);
        if (Objects.isNull(concurrentMap)) {
            return;
        }

        concurrentMap.remove(hostId);
    }

    public static void expirationElimination(String groupName, String hostId) {

    }

    @Override
    public void start() {
        LogUtils.info(log, "CacheRegisterTable start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    @Override
    public void close() {
        LogUtils.info(log, "CacheRegisterTable stop");
        CACHE.invalidateAll();
    }
}
