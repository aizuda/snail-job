package com.aizuda.easy.retry.server.enums;

import lombok.Getter;

/**
 * 状态值
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-26 17:58
 */
@Getter
public enum StatusEnum {

    NO(0),
    YES(1);

    private final Integer status;

    StatusEnum(int status) {
        this.status = status;
    }


}
