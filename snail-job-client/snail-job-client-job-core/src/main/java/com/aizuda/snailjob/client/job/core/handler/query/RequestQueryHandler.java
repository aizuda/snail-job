package com.aizuda.snailjob.client.job.core.handler.query;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.dto.JobResponseVO;
import com.aizuda.snailjob.client.job.core.handler.AbstractRequestHandler;
import com.aizuda.snailjob.common.core.util.JsonUtil;

import java.util.Objects;

public class RequestQueryHandler extends AbstractRequestHandler<JobResponseVO> {
    private final Long queryJobId;

    public RequestQueryHandler(Long queryJobId) {
        this.queryJobId = queryJobId;
    }

    @Override
    protected void afterExecute(JobResponseVO jobResponseVO) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected JobResponseVO doExecute() {
        Object data = client.getJobDetail(queryJobId).getData();
        Assert.isTrue(Objects.nonNull(data), () -> new SnailJobClientException("获取[{}]任务详情失败", queryJobId));
        return JsonUtil.parseObject(JsonUtil.toJsonString(data), JobResponseVO.class);
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(queryJobId != null && !Long.valueOf(0).equals(queryJobId), "queryJobId不能为null并且必须大于0");
    }

}
