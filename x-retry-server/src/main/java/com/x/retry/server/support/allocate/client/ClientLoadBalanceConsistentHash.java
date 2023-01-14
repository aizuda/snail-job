package com.x.retry.server.support.allocate.client;

import com.x.retry.common.core.enums.AllocationAlgorithmEnum;
import com.x.retry.server.support.allocate.common.ConsistentHashRouter;
import com.x.retry.server.support.allocate.common.Node;
import com.x.retry.server.support.ClientLoadBalance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-11 21:32
 */
public class ClientLoadBalanceConsistentHash implements ClientLoadBalance {

    private final int virtualNodeCnt;

    public ClientLoadBalanceConsistentHash(int virtualNodeCnt) {
        this.virtualNodeCnt = virtualNodeCnt;
    }

    @Override
    public String route(String currentGroupName, TreeSet<String> clientAllAddressSet) {

        Collection<ClientNode> cidNodes = new ArrayList<ClientNode>();
        for (String clientAddress : clientAllAddressSet) {
            cidNodes.add(new ClientNode(clientAddress));
        }
        final ConsistentHashRouter<ClientNode> consistentHashRouter = new ConsistentHashRouter<>(cidNodes, virtualNodeCnt);

        ClientNode clientNode = consistentHashRouter.routeNode(currentGroupName);

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
