package com.aizuda.easy.retry.client.core;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 参数表达式解析引擎
 *
 * @author www.byteblogs.com
 * @date 2023-09-10 12:30:23
 * @since 2.3.0
 */
public interface ExpressionEngine {

    /**
     * 执行表达式
     * @param expression 表达式
     *
     * @param args 参数信息
     * @param method 方法对象
     * @return 执行结果
     */
    Object eval(String expression, Object[] args, Method method);
}
