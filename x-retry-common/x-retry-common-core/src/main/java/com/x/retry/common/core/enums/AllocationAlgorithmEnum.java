package com.x.retry.common.core.enums;

import lombok.Getter;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-11 21:55
 */
@Getter
public enum AllocationAlgorithmEnum {

    CONSISTENT_HASH(1),
    RANDOM(2),
    LRU(3),
    ;

    private final int type;
    AllocationAlgorithmEnum(int type) {
        this.type = type;
    }
}
