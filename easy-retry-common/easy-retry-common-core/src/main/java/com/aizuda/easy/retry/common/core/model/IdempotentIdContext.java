package com.aizuda.easy.retry.common.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IdempotentIdContext {

    private String scene;

    private String targetClassName;

    private Object[] args;

    private String methodName;

}
