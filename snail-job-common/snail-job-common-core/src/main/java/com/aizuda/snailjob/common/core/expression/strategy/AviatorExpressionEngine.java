package com.aizuda.snailjob.common.core.expression.strategy;

import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;

import java.util.Map;

/**
 * Aviator 表达式
 *
 * @author opensnail
 * @date 2023-09-10 17:34:07
 * @since 2.3.0
 */
public class AviatorExpressionEngine extends AbstractExpressionEngine {

    private static final AviatorEvaluatorInstance ENGINE = AviatorEvaluator.getInstance();

    @Override
    protected Object doEval(String expression, Map<String, Object> context) {

        try {
            return ENGINE.execute(expression, context);
        } catch (Exception e) {
            throw new SnailJobCommonException("Aviator expression parsing exception. expression:[{}] context:[{}]",
                    expression, JsonUtil.toJsonString(context), e);
        }
    }
}
