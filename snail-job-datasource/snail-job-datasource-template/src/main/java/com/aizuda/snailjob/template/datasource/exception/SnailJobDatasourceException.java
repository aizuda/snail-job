package com.aizuda.snailjob.template.datasource.exception;

import com.aizuda.snailjob.common.core.exception.BaseSnailJobException;

/**
 * 数据源模块异常类
 *
 * @author: opensnail
 * @date : 2021-11-19 15:01
 */
public class SnailJobDatasourceException extends BaseSnailJobException {

    public SnailJobDatasourceException(String message) {
        super(message);
    }

    public SnailJobDatasourceException(String message, Object... arguments) {
        super(message, arguments);
    }
}
