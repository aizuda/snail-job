package com.aizuda.snailjob.common.core.enums;

import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author opensnail
 * @date 2023-10-02 10:39:22
 * @since 2.4.0
 */
@AllArgsConstructor
@Getter
public enum JobTaskTypeEnum {

    CLUSTER(1),
    BROADCAST(2),
    SHARDING(3);

    private final int type;

    public static JobTaskTypeEnum valueOf(int type) {
        for (JobTaskTypeEnum value : JobTaskTypeEnum.values()) {
            if (value.getType() == type) {
                return value;
            }
        }

        throw new SnailJobCommonException("未知类型");
    }
}
