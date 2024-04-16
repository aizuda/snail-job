package com.aizuda.snailjob.common.core.expression.strategy;

import com.aizuda.snailjob.common.core.expression.ExpressionEngine;

import java.util.Map;

/**
 * @author opensnail
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
