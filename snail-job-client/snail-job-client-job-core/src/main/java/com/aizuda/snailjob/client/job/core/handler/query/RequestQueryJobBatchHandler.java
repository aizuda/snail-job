package com.aizuda.snailjob.client.job.core.handler.query;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.dto.JobBatchResponseVO;
import com.aizuda.snailjob.client.job.core.handler.AbstractJobRequestHandler;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;

import java.util.Objects;

/**
 * @since 1.5.0
 */
public class RequestQueryJobBatchHandler extends AbstractJobRequestHandler<JobBatchResponseVO> {
    private final Long queryJobBatchId;

    public RequestQueryJobBatchHandler(Long queryJobBatchId) {
        this.queryJobBatchId = queryJobBatchId;
    }

    @Override
    protected void afterExecute(JobBatchResponseVO jobBatchResponseVO) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected JobBatchResponseVO doExecute() {
        Result<Object> result;
        if (isOpenApiV2()) {
            result = clientV2.getJobBatchDetail(queryJobBatchId);
        } else {
            result = client.getJobBatchDetail(queryJobBatchId);
        }

        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        Object data = result.getData();
        Assert.isTrue(Objects.nonNull(data), () -> new SnailJobClientException("Failed to get task batch details for [{}]", queryJobBatchId));
        return JsonUtil.parseObject(JsonUtil.toJsonString(data), JobBatchResponseVO.class);
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(queryJobBatchId != null && !Long.valueOf(0).equals(queryJobBatchId), "queryJobBatchId cannot be null and must be greater than 0");
    }

}
