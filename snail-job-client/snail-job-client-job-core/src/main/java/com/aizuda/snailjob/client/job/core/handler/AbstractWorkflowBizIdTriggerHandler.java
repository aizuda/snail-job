package com.aizuda.snailjob.client.job.core.handler;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.model.request.WorkflowTriggerBizIdRequest;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


/**
 * Abstract handler for workflow operations using bizId.
 *
 * @author opensnail
 * @since 1.10.0
 */
public abstract class AbstractWorkflowBizIdTriggerHandler<H, R> extends AbstractJobRequestHandler<R> {
    @Getter
    private final WorkflowTriggerBizIdRequest reqDTO;

    protected H r;

    public AbstractWorkflowBizIdTriggerHandler(String bizId) {
        this.reqDTO = new WorkflowTriggerBizIdRequest();
        reqDTO.setBizId(bizId);
    }

    /**
     * Set the fluent result object
     *
     * @param r the handler instance
     * @return the handler instance
     */
    protected H setR(H r) {
        this.r = r;
        return r;
    }

    /**
     * Add context to the workflow
     *
     * @param argsKey   parameter key
     * @param argsValue parameter value
     * @return the handler instance
     */
    protected H addContext(String argsKey, Object argsValue) {
        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(reqDTO.getTmpWfContext())) {
            map = JsonUtil.parseHashMap(reqDTO.getTmpWfContext());
        }
        map.put(argsKey, argsValue);
        reqDTO.setTmpWfContext(JsonUtil.toJsonString(map));
        return r;
    }

    protected abstract void afterExecute(Boolean aBoolean);
}