package com.aizuda.easy.retry.server.common.handler;

import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.ClientLoadBalance;
import com.aizuda.easy.retry.server.common.allocate.client.ClientLoadBalanceManager;
import com.aizuda.easy.retry.server.common.allocate.client.ClientLoadBalanceManager.AllocationAlgorithmEnum;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: www.byteblogs.com
 * @date : 2023-01-10 14:18
 */
@Component
@RequiredArgsConstructor
public class ClientNodeAllocateHandler {
    private final AccessTemplate accessTemplate;

    /**
     * 获取分配的节点
     *
     * @param allocKey  分配的key
     * @param groupName 组名称
     * @param routeKey {@link AllocationAlgorithmEnum} 路由类型
     */
    public RegisterNodeInfo getServerNode(String allocKey, String groupName, String namespaceId, Integer routeKey) {

        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(groupName, namespaceId);
        if (CollectionUtils.isEmpty(serverNodes)) {
            EasyRetryLog.LOCAL.warn("client node is null. groupName:[{}]", groupName);
            return null;
        }

        ClientLoadBalance clientLoadBalanceRandom = ClientLoadBalanceManager.getClientLoadBalance(routeKey);

        String hostId = clientLoadBalanceRandom.route(allocKey, new TreeSet<>(serverNodes.stream().map(RegisterNodeInfo::getHostId).collect(Collectors.toSet())));

        Stream<RegisterNodeInfo> registerNodeInfoStream = serverNodes.stream()
            .filter(s -> s.getHostId().equals(hostId));
        return registerNodeInfoStream.findFirst().orElse(null);
    }



}
