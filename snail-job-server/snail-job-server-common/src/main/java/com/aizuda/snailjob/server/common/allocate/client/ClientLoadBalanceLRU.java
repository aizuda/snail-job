package com.aizuda.snailjob.server.common.allocate.client;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.server.common.ClientLoadBalance;
import com.aizuda.snailjob.server.common.allocate.client.ClientLoadBalanceManager.AllocationAlgorithmEnum;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: opensnail
 * @date : 2022-03-12 09:55
 */
public class ClientLoadBalanceLRU implements ClientLoadBalance {

    private int size;

    public ClientLoadBalanceLRU(int size) {
        this.size = size;
    }

    private ConcurrentHashMap<String, LinkedHashMap<String, String>> LRU_CACHE = new ConcurrentHashMap<>();

    @Override
    public String route(String allocKey, TreeSet<String> clientAllAddressSet) {
        LinkedHashMap<String, String> lruItem = LRU_CACHE.get(allocKey);
        if (Objects.isNull(lruItem)) {
            lruItem = new LinkedHashMap<String, String>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return super.size() > size;
                }
            };

            LRU_CACHE.put(allocKey, lruItem);
        }

        // 添加新数据
        for (String address : clientAllAddressSet) {
            lruItem.computeIfAbsent(address, key -> address);
        }

        // 删除已经下线的节点
        List<String> delKeys = new ArrayList<>();
        for (String address : lruItem.keySet()) {
            if (!clientAllAddressSet.contains(address)) {
                delKeys.add(address);
            }
        }
        if (CollectionUtil.isNotEmpty(delKeys)) {
            for (String delKey : delKeys) {
                lruItem.remove(delKey);
            }
        }

        return lruItem.values().stream().findFirst().get();
    }

    @Override
    public int routeType() {
        return AllocationAlgorithmEnum.LRU.getType();
    }
}
