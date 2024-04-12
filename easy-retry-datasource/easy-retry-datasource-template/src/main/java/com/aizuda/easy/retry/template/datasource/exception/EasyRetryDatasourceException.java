package com.aizuda.easy.retry.template.datasource.exception;

import com.aizuda.easy.retry.common.core.exception.BaseEasyRetryException;

/**
 * 数据源模块异常类
 *
 * @author: opensnail
 * @date : 2021-11-19 15:01
 */
public class EasyRetryDatasourceException extends BaseEasyRetryException {

    public EasyRetryDatasourceException(String message) {
        super(message);
    }

    public EasyRetryDatasourceException(String message, Object ... arguments) {
        super(message, arguments);
    }
}
