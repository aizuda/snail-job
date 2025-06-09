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


    RUNNING(1,"running"),
    DOWN(2, "down"),
    ;

    private final Integer type;
    private final String status;

}
