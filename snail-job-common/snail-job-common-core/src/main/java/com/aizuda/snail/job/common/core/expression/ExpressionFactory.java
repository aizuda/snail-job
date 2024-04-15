package com.aizuda.snail.job.common.core.expression;

import com.aizuda.snail.job.common.core.exception.EasyRetryCommonException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author xiaowoniu
 * @date 2023-12-30 17:56:58
 * @since 2.6.0
 */
public class ExpressionFactory {

    /**
     * 返回一个代理对象
     *
     * @param invocationHandler 表达式执行的代理对象
     * @return 返回一个代理对象
     */
    public static ExpressionEngine getExpressionEngine(InvocationHandler invocationHandler) {

        try {
            return (ExpressionEngine) Proxy.newProxyInstance(ExpressionEngine.class.getClassLoader(),
                    new Class[]{ExpressionEngine.class}, invocationHandler);
        } catch (Exception e) {
            throw new EasyRetryCommonException("class not found exception to: [{}]", e);

        }
    }
}
