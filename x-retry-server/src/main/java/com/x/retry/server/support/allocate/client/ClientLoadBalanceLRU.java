package com.x.retry.server.support.allocate.client;

import com.x.retry.server.support.ClientLoadBalance;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-12 09:55
 */
public class ClientLoadBalanceLRU implements ClientLoadBalance {

    private int size;

    public ClientLoadBalanceLRU(int size) {
        this.size = size;
    }

    private ConcurrentHashMap<String, LinkedHashMap<String, String>> LRU_CACHE = new ConcurrentHashMap<>();

    @Override
    public String route(String currentGroupName, TreeSet<String> clientAllAddressSet) {
        LinkedHashMap<String, String> lruItem = LRU_CACHE.get(currentGroupName);
        if (Objects.isNull(lruItem)) {
            lruItem = new LinkedHashMap<String, String>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return super.size() > size;
                }
            };
        }

        // 添加新数据
        for (String address: clientAllAddressSet) {
            if (!lruItem.containsKey(address)) {
                lruItem.put(address, address);
            }
        }

        // 删除已经下线的节点
        for (String address : lruItem.keySet()) {
            if (!clientAllAddressSet.contains(address)) {
                lruItem.remove(address);
            }
        }

        return lruItem.values().stream().findFirst().get();
    }

    @Override
    public int routeType() {
        return 0;
    }
}
