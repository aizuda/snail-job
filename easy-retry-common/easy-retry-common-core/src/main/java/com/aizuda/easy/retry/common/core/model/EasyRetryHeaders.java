package com.aizuda.easy.retry.common.core.model;

import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import lombok.Data;

/**
 * easy-retry 请求头信息
 *
 * @author: opensnail
 * @date : 2022-04-16 22:20
 */
@Data
public class EasyRetryHeaders {

    /**
     * 是否是重试流量
     */
    private boolean easyRetry;

    /**
     * 重试下发的ID
     */
    private String easyRetryId;

    /**
     * 调用链超时时间 单位毫秒(ms)
     */
    private long ddl = SystemConstants.DEFAULT_DDL;
}
