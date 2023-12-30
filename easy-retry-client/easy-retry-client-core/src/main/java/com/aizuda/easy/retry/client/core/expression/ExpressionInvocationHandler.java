package com.aizuda.easy.retry.client.core.expression;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaowoniu
 * @date 2023-12-30 17:22:51
 * @since 2.6.0
 */
public class ExpressionInvocationHandler implements InvocationHandler {
    private static final DefaultParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();

    private final Object expressionEngine;

    public ExpressionInvocationHandler(Object expressionEngine) {
        this.expressionEngine = expressionEngine;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 表达式
        String expression = (String) args[0];
        if (StrUtil.isBlank(expression)) {
            return StrUtil.EMPTY;
        }

        // 表达式参数 params => 0: 重试方法的参数, 1: 重试方法
        Object[] params = (Object[])args[1];
        // 获取参数名称
        String[] paramNameArr = DISCOVERER.getParameterNames((Method) params[1]);
        if (ArrayUtil.isEmpty(paramNameArr)) {
            return null;
        }

        // 重试方法的参数
        Object[] methodArgs =  (Object[]) params[0];
        Map<String, Object> context = new HashMap<>(methodArgs.length);
        for (int i = 0; i < paramNameArr.length; i++) {
            context.put(paramNameArr[i], methodArgs[i]);
        }

        return method.invoke(expression, context);
    }
}
