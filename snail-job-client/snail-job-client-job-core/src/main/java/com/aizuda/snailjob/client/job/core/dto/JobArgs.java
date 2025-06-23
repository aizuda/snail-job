package com.aizuda.snailjob.client.job.core.dto;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;

/**
 * @author: opensnail
 * @date : 2023-10-18 16:53
 * @since : 2.4.0
 */
@Data
public class JobArgs {

    private Object jobParams;

    private String executorInfo;

    private Long taskBatchId;

    private Long workflowTaskBatchId;

    private Long workflowNodeId;

    private Long jobId;

    private Map<String, Object> wfContext;

    private Map<String, Object> changeWfContext;

    public void appendContext(String key, Object value) {
        if (Objects.isNull(wfContext) || StrUtil.isBlank(key) || Objects.isNull(value)) {
            return;
        }

        changeWfContext.put(key, value);
    }

    public Object getWfContext(String key) {
        if (Objects.isNull(wfContext) || StrUtil.isBlank(key)) {
            return null;
        }

        return wfContext.get(key);
    }

}
