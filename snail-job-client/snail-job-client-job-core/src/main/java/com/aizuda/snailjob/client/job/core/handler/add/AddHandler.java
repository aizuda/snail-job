package com.aizuda.snailjob.client.job.core.handler.add;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.handler.AbstractParamsHandler;
import com.aizuda.snailjob.client.common.util.ValidatorUtils;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;

import static com.aizuda.snailjob.client.job.core.enums.TriggerTypeEnum.WORK_FLOW;

public abstract class AddHandler<H> extends AbstractParamsHandler<H, Long> {

    public AddHandler(JobTaskTypeEnum taskType) {
        super(taskType);
    }

    @Override
    protected Long doExecute() {
        Result<Object> result;
        if (isOpenApiV2()) {
            result = clientV2.addJob(getReqDTO());
        } else {
            result = client.addJob(getReqDTO());
        }
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        String data = JsonUtil.toJsonString(result.getData());
        return Long.valueOf(data);
    }

    @Override
    protected void beforeExecute() {
        // 此次是兜底覆盖,工作流是没有调度时间
        if (getReqDTO().getTriggerType() == WORK_FLOW.getType()) {
            setTriggerInterval("*");
        }
    }

    @Override
    protected void afterExecute(Long id) {

    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(Add.class, getReqDTO());
    }

}
