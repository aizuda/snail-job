package com.aizuda.easy.retry.common.core.expression.strategy;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.expression.ExpressionEngine;
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
    @Override
    public Object eval(String expression, Object... t) {

        return doEval(expression, (Map<String, Object>) t[0]);
    }

    protected abstract Object doEval(String expression, Map<String, Object> context);
}
