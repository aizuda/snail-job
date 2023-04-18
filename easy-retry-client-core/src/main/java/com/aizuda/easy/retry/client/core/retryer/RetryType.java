package com.aizuda.easy.retry.client.core.retryer;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 15:14
 */
public enum RetryType {

    /**
     * 本地重试
     */
    ONLY_LOCAL,

    /**
     * 远程重试
     */
    ONLY_REMOTE,

    /**
     * 先本地重试，在远程重试
     */
    LOCAL_REMOTE
}
