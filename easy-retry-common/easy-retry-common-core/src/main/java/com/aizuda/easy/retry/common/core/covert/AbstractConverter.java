package com.aizuda.easy.retry.common.core.covert;

import com.aizuda.easy.retry.common.core.exception.XRetryCommonException;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-26 11:42
 */
public abstract class AbstractConverter<S, T> implements Converter<S, T> {

    protected T convert(S s, Class<T> c) {
        T t;
        try {
            t = c.newInstance();
        } catch (Exception e) {
            throw new XRetryCommonException("创建对象失败");
        }

        BeanUtils.copyProperties(s, t);
        return t;
    }

    protected T convert(S s, T t) {
        BeanUtils.copyProperties(s, t);
        return t;
    }

    protected List<T> batchConvert(List<S> sList, Class<T> c) {

        List<T> list = new ArrayList<>(sList.size());

        if (CollectionUtils.isEmpty(sList)) {
            return list;
        }

        for (S s : sList) {
            list.add(convert(s, c));
        }

        return list;
    }
}
