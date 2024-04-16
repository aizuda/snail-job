package com.aizuda.snailjob.client.core.annotation;

/**
 * Snail Job 重试任务传播机制
 *
 * @author: xiaowoniu
 * @date : 2024-02-05
 * @since : 3.1.0
 */
public enum Propagation {

    /**
     * 当设置为REQUIRED时，如果当前重试存在，就加入到当前重试中，即外部入口触发重试
     * 如果当前重试不存在，就创建一个新的重试任务。
     */
    REQUIRED,

    /**
     * 当设置为REQUIRES_NEW时，
     * 无论当前重试任务是否存在，都会一个新的重试任务。
     */
    REQUIRES_NEW
    ;

}
