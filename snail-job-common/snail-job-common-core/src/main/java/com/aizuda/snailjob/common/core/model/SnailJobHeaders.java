package com.aizuda.snailjob.common.core.model;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import lombok.Data;

/**
 * snail-job 请求头信息
 *
 * @author: opensnail
 * @date : 2022-04-16 22:20
 */
@Data
public class SnailJobHeaders {

    /**
     * 是否是重试流量
     */
    private boolean isRetry;

    /**
     * 重试下发的ID
     */
    private String retryId;

    /**
     * 调用链超时时间 单位毫秒(ms)
     */
    private long ddl = SystemConstants.DEFAULT_DDL;
}
