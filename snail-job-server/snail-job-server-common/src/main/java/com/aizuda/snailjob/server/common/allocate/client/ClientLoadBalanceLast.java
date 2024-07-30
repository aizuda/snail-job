package com.aizuda.snailjob.server.common.allocate.client;

import com.aizuda.snailjob.server.common.ClientLoadBalance;

import java.util.TreeSet;

public class ClientLoadBalanceLast implements ClientLoadBalance {
    @Override
    public String route(String key, TreeSet<String> clientAllAddressSet) {
        return clientAllAddressSet.last();
    }

    @Override
    public int routeType() {
        return ClientLoadBalanceManager.AllocationAlgorithmEnum.LAST.getType();
    }
}
