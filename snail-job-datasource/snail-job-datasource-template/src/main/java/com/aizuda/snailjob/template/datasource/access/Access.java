package com.aizuda.snailjob.template.datasource.access;


/**
 * @author opensnail
 * @date 2023-07-20 22:47:57
 * @since 2.1.0
 */
public interface Access<T> {

    boolean supports(String operationType);
}
