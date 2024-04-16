package com.aizuda.snailjob.common.core.enums;

import lombok.Getter;

/**
 * 服务节点类型
 *
 * @author: opensnail
 * @date : 2021-11-26 18:01
 */
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

    NodeTypeEnum(int type) {
        this.type = type;
    }

}
