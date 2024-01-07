package com.aizuda.easy.retry.server.job.task.support.expression;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.util.JsonUtil;

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

    private final Object expressionEngine;

    public ExpressionInvocationHandler(Object expressionEngine) {
        this.expressionEngine = expressionEngine;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object[] expressionParams = (Object[]) args[1];
        String params = (String) expressionParams[0];
        Map<String, Object> contextMap = new HashMap<>();
        if (StrUtil.isNotBlank(params)) {
            contextMap = JsonUtil.parseHashMap(params);
        }

        args[1] = new Object[]{contextMap};
        return method.invoke(expressionEngine, args);
    }
}
