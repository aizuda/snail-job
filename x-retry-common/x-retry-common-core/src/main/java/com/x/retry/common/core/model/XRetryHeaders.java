package com.x.retry.common.core.model;

import lombok.Data;

/**
 * x-retry 请求头信息
 *
 * @author: www.byteblogs.com
 * @date : 2022-04-16 22:20
 */
@Data
public class XRetryHeaders {

    /**
     * 是否是重试流量
     */
    private boolean xRetry;

    /**
     * 重试下发的ID
     */
    private String xRetryId;

    /**
     * 调用链超时时间 单位毫秒(ms)
     */
    private long ddl = 60 * 10 * 1000;
}
