package com.x.retry.common.core.enums;

import lombok.Getter;

/**
 * 重试状态终态枚举
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-03 11:05
 */
@Getter
public enum RetryStatusEnum {

    /**
     * 重试中
     */
    RUNNING(0),

    /**
     * 重试完成
     */
    FINISH(1),

    /**
     * 到达最大重试次数
     */
    MAX_RETRY_COUNT(2);

    private final Integer level;

    RetryStatusEnum(int level) {
        this.level = level;
    }

}
