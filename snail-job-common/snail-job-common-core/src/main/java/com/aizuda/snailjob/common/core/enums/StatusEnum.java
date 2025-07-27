package com.aizuda.snailjob.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态值
 *
 * @author: opensnail
 * @date : 2021-11-26 17:58
 */
@AllArgsConstructor
@Getter
public enum StatusEnum {

    NO(0),
    YES(1);

    private final Integer status;

    public static StatusEnum of(Integer status) {
        for (StatusEnum statusEnum : StatusEnum.values()) {
            if (statusEnum.status.equals(status)) {
                return statusEnum;
            }
        }
        return null;
    }

}
