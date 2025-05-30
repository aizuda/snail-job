package com.aizuda.snailjob.client.job.core.handler.query;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.dto.WorkflowDetailResponseVO;
import com.aizuda.snailjob.client.job.core.handler.AbstractJobRequestHandler;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;

import java.util.Objects;

/**
 * @since 1.5.0
 */
public class RequestQueryWorkflowBatchHandler extends AbstractJobRequestHandler<WorkflowDetailResponseVO> {
    private final Long workflowBatchId;

    public RequestQueryWorkflowBatchHandler(Long workflowBatchId) {
        this.workflowBatchId = workflowBatchId;
    }

    @Override
    protected void afterExecute(WorkflowDetailResponseVO workflowDetailResponseVO) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected WorkflowDetailResponseVO doExecute() {
        Result<Object> result = client.getWorkflowBatchDetail(workflowBatchId);
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        Object data = result.getData();
        Assert.isTrue(Objects.nonNull(data), () -> new SnailJobClientException("Failed to get workflow task batch details for [{}]", workflowBatchId));
        return JsonUtil.parseObject(JsonUtil.toJsonString(data), WorkflowDetailResponseVO.class);
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(workflowBatchId != null && !Long.valueOf(0).equals(workflowBatchId), "queryJobBatchId cannot be null and must be greater than 0");
    }

}
