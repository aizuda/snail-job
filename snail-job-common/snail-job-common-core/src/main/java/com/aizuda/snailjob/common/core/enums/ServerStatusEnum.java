package com.aizuda.snailjob.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务节点类型
 *
 * @since 1.6.0
 */
@AllArgsConstructor
@Getter
public enum ServerStatusEnum {
    UP(1,"up"),
    DOWN(2, "down"),
    ;

    private final Integer type;
    private final String status;

    public static ServerStatusEnum getByType(Integer type) {
        for (ServerStatusEnum serverStatusEnum : ServerStatusEnum.values()) {
            if (serverStatusEnum.getType().equals(type)) {
                return serverStatusEnum;
            }
        }
        return null;
    }
}
