package com.aizuda.easy.retry.client.core.expression;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.client.core.ExpressionEngine;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author www.byteblogs.com
 * @date 2023-09-10 12:31:17
 * @since 2.3.0
 */
public abstract class AbstractExpressionEngine implements ExpressionEngine {
    private static final DefaultParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();

    @Override
    public Object eval(String expression, Object[] args, Method method) {

        if (StringUtils.isBlank(expression)) {
            return StrUtil.EMPTY;
        }

        // 获取参数名称
        String[] paramNameArr = DISCOVERER.getParameterNames(method);
        if (ArrayUtils.isEmpty(paramNameArr)) {
            return null;
        }

        Map<String, Object> context = new HashMap<>(args.length);
        for (int i = 0; i < paramNameArr.length; i++) {
            context.put(paramNameArr[i], args[i]);
        }

        return doEval(expression, context);
    }

    protected abstract Object doEval(String expression, Map<String, Object> context);
}
