package com.aizuda.snailjob.server.common.handler;

import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.ClientLoadBalance;
import com.aizuda.snailjob.server.common.allocate.client.ClientLoadBalanceManager;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * @author: opensnail
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
     * @param routeKey  {@link ClientLoadBalanceManager.AllocationAlgorithmEnum} 路由类型
     */
    public RegisterNodeInfo getServerNode(String allocKey, String groupName, String namespaceId, Integer routeKey) {

        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(groupName, namespaceId);
        if (CollectionUtils.isEmpty(serverNodes)) {
            SnailJobLog.LOCAL.warn("client node is null. groupName:[{}]", groupName);
            return null;
        }

        ClientLoadBalance clientLoadBalanceRandom = ClientLoadBalanceManager.getClientLoadBalance(routeKey);

        String hostId = clientLoadBalanceRandom.route(allocKey, new TreeSet<>(StreamUtils.toSet(serverNodes, RegisterNodeInfo::getHostId)));

        Stream<RegisterNodeInfo> registerNodeInfoStream = serverNodes.stream()
            .filter(s -> s.getHostId().equals(hostId));
        return registerNodeInfoStream.findFirst().orElse(null);
    }
    
}
