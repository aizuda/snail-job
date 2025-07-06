package com.aizuda.snailjob.server.common.dto;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: opensnail
 * @date : 2023-09-21 09:26
 * since: 2.4.0
 */
public class DistributeInstance {
    private DistributeInstance() {
    }

    public static final DistributeInstance INSTANCE = new DistributeInstance();

    /**
     * 控制rebalance状态
     */
    public static final AtomicBoolean RE_BALANCE_ING = new AtomicBoolean(Boolean.FALSE);

    private final CopyOnWriteArraySet<Integer> CONSUMER_BUCKETS = new CopyOnWriteArraySet<>();

    public CopyOnWriteArraySet<Integer> getConsumerBucket() {
        return CONSUMER_BUCKETS;
    }

    public void setConsumerBucket(List<Integer> buckets) {
        CONSUMER_BUCKETS.addAll(buckets);
    }

    public void clearConsumerBucket() {
        CONSUMER_BUCKETS.clear();
    }

}
