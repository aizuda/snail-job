package com.aizuda.easy.retry.common.core.expression;

/**
 * 参数表达式解析引擎
 *
 * @author opensnail
 * @date 2023-09-10 12:30:23
 * @since 2.3.0
 */
public interface ExpressionEngine{

    /**
     * 执行表达式
     * @param expression 表达式
     *
     * @param t 参数信息
     * @return 执行结果
     */
    Object eval(String expression, Object ...t);
}
