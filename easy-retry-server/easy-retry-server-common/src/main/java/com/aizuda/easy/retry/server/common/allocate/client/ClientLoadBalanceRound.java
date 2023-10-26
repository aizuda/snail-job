package com.aizuda.easy.retry.server.common.allocate.client;

import com.aizuda.easy.retry.server.common.ClientLoadBalance;
import com.aizuda.easy.retry.server.common.allocate.client.ClientLoadBalanceManager.AllocationAlgorithmEnum;

import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-13 15:43
 * @since : 2.4.0
 */
public class ClientLoadBalanceRound implements ClientLoadBalance  {

    private static final ConcurrentHashMap<String, AtomicInteger> COUNTER = new ConcurrentHashMap<>();
    private static final int THRESHOLD = Integer.MAX_VALUE - 10000;

    @Override
    public String route(final String allocKey, final TreeSet<String> clientAllAddressSet) {
        String[] addressArr = clientAllAddressSet.toArray(new String[0]);
        AtomicInteger next = COUNTER.getOrDefault(allocKey, new AtomicInteger(1));
        String nextClientId = addressArr[next.get() % clientAllAddressSet.size()];
        int nextIndex = next.incrementAndGet();
        if (nextIndex > THRESHOLD) {
            next = new AtomicInteger(1);
        }

        COUNTER.put(allocKey, next);
        return nextClientId;
    }

    @Override
    public int routeType() {
        return AllocationAlgorithmEnum.ROUND.getType();
    }
}
