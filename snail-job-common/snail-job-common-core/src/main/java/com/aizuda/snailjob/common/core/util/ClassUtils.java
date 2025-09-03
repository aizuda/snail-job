package com.aizuda.snailjob.common.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-09-03
 */
public class ClassUtils {

    /**
     * 获取方法的返回值类型
     *
     * @param method Method
     * @return
     */
    public static Class<?> getReturnType(Method method) {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            return (Class<?>) actualTypeArguments[0];
        }
        return Object.class;
    }
}
