package com.aizuda.easy.retry.client.core.spel;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 17:27
 */
public class SPELParamFunction implements Function<Object[], String> {

    private static DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    private String bizNo;

    private Method method;

    public SPELParamFunction(String bizNo, Method method) {
        this.bizNo = bizNo;
        this.method = method;
    }

    @Override
    public String apply(Object[] params) {

        if (StringUtils.isBlank(bizNo)) {
            return StrUtil.EMPTY;
        }

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] paramNameArr = discoverer.getParameterNames(method);
        if (ArrayUtils.isEmpty(paramNameArr)) {
            return null;
        }

        for (int i = 0; i < paramNameArr.length; i++) {
            context.setVariable(paramNameArr[i], params[i]);
        }

        return parser.parseExpression(bizNo).getValue(context, String.class);

    }
}
