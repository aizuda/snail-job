package com.aizuda.snailjob.server.job.task.support.expression;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
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
    public Object invoke(Object proxy, Method method, Object[] args) {

        try {
            Object[] expressionParams = (Object[]) args[1];
            String params = (String) expressionParams[0];
            Map<String, Object> contextMap = new HashMap<>();
            if (StrUtil.isNotBlank(params)) {
                try {
                    contextMap = JsonUtil.parseHashMap(params);
                } catch (Exception e) {
                    contextMap.put(SystemConstants.SINGLE_PARAM, params);
                }
            }

            args[1] = new Object[]{contextMap};
            return method.invoke(expressionEngine, args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            throw new SnailJobServerException(targetException.getMessage());
        } catch (Exception e) {
            throw new SnailJobServerException("Expression execution failed", e);
        }
    }
}
