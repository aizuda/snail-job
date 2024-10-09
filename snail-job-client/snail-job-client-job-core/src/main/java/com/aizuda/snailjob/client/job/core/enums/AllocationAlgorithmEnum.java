package com.aizuda.snailjob.client.job.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AllocationAlgorithmEnum {
    // Hash
    CONSISTENT_HASH(1),
    // 随机
    RANDOM(2),
    // LRU
    LRU(3),
    // 轮询
    ROUND(4),
    // 匹配第一个
    FIRST(5),
    // 匹配最后一个
    LAST(6);

    private final int type;

}