package com.aizuda.snailjob.server.common.enums;

import com.aizuda.snailjob.common.core.expression.ExpressionEngine;
import com.aizuda.snailjob.common.core.expression.strategy.AviatorExpressionEngine;
import com.aizuda.snailjob.common.core.expression.strategy.QLExpressEngine;
import com.aizuda.snailjob.common.core.expression.strategy.SpELExpressionEngine;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xiaowoniu
 * @date 2023-12-30 10:50:05
 * @since 2.6.0
 */
@Getter
@AllArgsConstructor
public enum ExpressionTypeEnum {
    SPEL(1, new SpELExpressionEngine()),
    AVIATOR(2, new AviatorExpressionEngine()),
    QL(3, new QLExpressEngine());

    private final Integer type;
    private final ExpressionEngine expressionEngine;

    public static ExpressionEngine valueOf(Integer type) {
        for (ExpressionTypeEnum expressionTypeEnum : ExpressionTypeEnum.values()) {
            if (expressionTypeEnum.getType().equals(type)) {
                return expressionTypeEnum.getExpressionEngine();
            }
        }

        return null;
    }

}
