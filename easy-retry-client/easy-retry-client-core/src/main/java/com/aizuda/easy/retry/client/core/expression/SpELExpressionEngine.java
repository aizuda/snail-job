package com.aizuda.easy.retry.client.core.expression;

import com.aizuda.easy.retry.client.core.exception.EasyRetryClientException;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * Spel表达式解析引擎
 *
 * @author www.byteblogs.com
 * @date 2023-09-10 12:36:56
 * @since 2.3.0
 */
public class SpELExpressionEngine extends AbstractExpressionEngine {

    private final static ExpressionParser ENGINE = new SpelExpressionParser();

    @Override
    protected Object doEval(String expression, Map<String, Object> context) {

        try {
            final EvaluationContext evaluationContext = new StandardEvaluationContext();
            context.forEach(evaluationContext::setVariable);
            return ENGINE.parseExpression(expression).getValue(evaluationContext, String.class);
        } catch (Exception e) {
            throw new EasyRetryClientException("SpEL表达式解析异常. expression:[{}] context:[{}]",
                    expression, JsonUtil.toJsonString(context), e);
        }

    }
}
