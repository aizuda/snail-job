package com.x.retry.common.core.model;

import lombok.Data;

/**
 * @author: shuguang.zhang
 * @date : 2022-04-16 22:20
 */
@Data
public class XRetryHeaders {

    /**
     * 是否是重试流量
     */
    private boolean xRetry;
}
