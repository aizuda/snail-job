package com.aizuda.snailjob.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务节点类型
 *
 * @author: opensnail
 * @date : 2021-11-26 18:01
 */
@AllArgsConstructor
@Getter
public enum NodeTypeEnum {

    /**
     * 客户端
     */
    CLIENT(1),

    /**
     * 服务端
     */
    SERVER(2),
    ;

    private final Integer type;

}
