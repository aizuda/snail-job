package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author opensnail
 * @date 2024-07-01 21:53:45
 * @since sj_1.1.0
 */
@Data
public class CheckDecisionVO {

    /**
     * 表达式类型 1、SpEl、2、Aviator 3、QL
     */
    @NotNull
    private Integer expressionType;

    /**
     * 条件节点表达式
     */
    @NotBlank
    private String nodeExpression;

    /**
     * 决策节点校验内容
     */
    private String checkContent;
}
