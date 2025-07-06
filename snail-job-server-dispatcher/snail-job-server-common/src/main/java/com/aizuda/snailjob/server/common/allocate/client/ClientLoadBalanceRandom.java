package com.aizuda.snailjob.server.common.allocate.client;

import com.aizuda.snailjob.server.common.ClientLoadBalance;

import java.util.Random;
import java.util.TreeSet;

/**
 * @author: opensnail
 * @date : 2022-03-11 22:00
 */
public class ClientLoadBalanceRandom implements ClientLoadBalance {

    private Random random = new Random();

    @Override
    public String route(String allocKey, TreeSet<String> clientAllAddressSet) {
        String[] addressArr = clientAllAddressSet.toArray(new String[clientAllAddressSet.size()]);
        return addressArr[random.nextInt(clientAllAddressSet.size())];
    }

    @Override
    public int routeType() {
        return ClientLoadBalanceManager.AllocationAlgorithmEnum.RANDOM.getType();
    }
}
