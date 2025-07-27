package com.aizuda.snailjob.model.request;

import lombok.Data;

/**
 * 工作流回调节点参数模型
 *
 * @author: xiaowoniu
 * @date : 2024-01-02
 * @since : 2.6.0
 */
@Data
@Deprecated
public class CallbackParamsRequest {

    /**
     * 工作流上下文
     */
    private String wfContext;

}
