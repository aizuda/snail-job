package com.x.retry.common.core.covert;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-26 11:39
 */
public interface Converter<S, T> {

    /**
     * 装换器
     *
     * @param s
     * @return
     */
    T convert(S s);

    List<T> batchConvert(List<S> sList);

}
