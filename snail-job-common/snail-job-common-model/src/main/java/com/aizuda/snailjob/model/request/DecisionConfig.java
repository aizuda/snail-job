package com.aizuda.snailjob.model.request;

import lombok.Data;

/**
 * 决策节点配置
 *
 * @author xiaowoniu
 * @date 2023-12-30 11:17:30
 * @since 2.6.0
 */
@Data
public class DecisionConfig {

    /**
     * 表达式类型 1、SpEl、2、Aviator 3、QL
     */
    private Integer expressionType;

    /**
     * 条件节点表达式
     */
    private String nodeExpression;

    /**
     * 是否为其他情况
     */
    private Integer defaultDecision;

}
