package com.x.retry.common.core.enums;

import lombok.Getter;

/**
 * 存储类型
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-26 18:01
 */
@Getter
public enum StorageTypeEnum {

    MYSQL(1),
    REDIS(2);

    private final Integer type;

    StorageTypeEnum(int type) {
        this.type = type;
    }


}
