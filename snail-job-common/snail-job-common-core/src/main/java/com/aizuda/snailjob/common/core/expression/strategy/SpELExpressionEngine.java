package com.aizuda.snailjob.common.core.expression.strategy;

import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * Spel表达式解析引擎
 *
 * @author opensnail
 * @date 2023-09-10 12:36:56
 * @since 2.3.0
 */
public class SpELExpressionEngine extends AbstractExpressionEngine {

    private static final ExpressionParser ENGINE = new SpelExpressionParser();

    @Override
    protected Object doEval(String expression, Map<String, Object> context) {

        try {
            final EvaluationContext evaluationContext = new StandardEvaluationContext();
            context.forEach(evaluationContext::setVariable);
            return ENGINE.parseExpression(expression).getValue(evaluationContext, Object.class);
        } catch (Exception e) {
            throw new SnailJobCommonException("SpEL表达式解析异常. expression:[{}] context:[{}]",
                    expression, JsonUtil.toJsonString(context), e);
        }

    }
}
