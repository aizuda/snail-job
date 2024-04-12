package com.aizuda.easy.retry.server.common.allocate.client;

import com.aizuda.easy.retry.server.common.ClientLoadBalance;
import com.aizuda.easy.retry.server.common.allocate.client.ClientLoadBalanceManager.AllocationAlgorithmEnum;
import com.aizuda.easy.retry.server.common.allocate.common.ConsistentHashRouter;
import com.aizuda.easy.retry.server.common.allocate.common.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author: opensnail
 * @date : 2022-03-11 21:32
 */
public class ClientLoadBalanceConsistentHash implements ClientLoadBalance {

    private final int virtualNodeCnt;

    public ClientLoadBalanceConsistentHash(int virtualNodeCnt) {
        this.virtualNodeCnt = virtualNodeCnt;
    }

    @Override
    public String route(String allocKey, TreeSet<String> clientAllAddressSet) {

        Collection<ClientNode> cidNodes = new ArrayList<ClientNode>();
        for (String clientAddress : clientAllAddressSet) {
            cidNodes.add(new ClientNode(clientAddress));
        }
        final ConsistentHashRouter<ClientNode> consistentHashRouter = new ConsistentHashRouter<>(cidNodes, virtualNodeCnt);

        ClientNode clientNode = consistentHashRouter.routeNode(allocKey);

        return clientNode.clientAddress;
    }

    @Override
    public int routeType() {
        return AllocationAlgorithmEnum.CONSISTENT_HASH.getType();
    }

    private static class ClientNode implements Node {
        private final String clientAddress;

        public ClientNode(String clientAddress) {
            this.clientAddress = clientAddress;
        }

        @Override
        public String getKey() {
            return clientAddress;
        }
    }
}
