package com.aizuda.snailjob.client.job.core.handler;


import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.dto.JobResponseVO;
import com.aizuda.snailjob.common.core.util.JsonUtil;

import java.util.Objects;

public class RequestQueryHandler extends AbstractRequestHandler<JobResponseVO> {
    private Long queryJobId;

    public RequestQueryHandler(Long queryJobId) {
        this.queryJobId = queryJobId;
    }

    @Override
    protected JobResponseVO doExecute() {
        Object data = client.getJobDetail(queryJobId).getData();
        Assert.isTrue(Objects.nonNull(data),()-> new SnailJobClientException("获取[{}]任务详情失败", queryJobId));
        return JsonUtil.parseObject(JsonUtil.toJsonString(data), JobResponseVO.class);
    }

    @Override
    protected boolean checkRequest() {
        return queryJobId != null && ! Long.valueOf(0).equals(queryJobId);
    }

}
