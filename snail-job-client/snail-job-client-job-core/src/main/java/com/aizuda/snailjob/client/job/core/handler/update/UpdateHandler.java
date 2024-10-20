package com.aizuda.snailjob.client.job.core.handler.update;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.enums.TriggerTypeEnum;
import com.aizuda.snailjob.client.job.core.handler.AbstractParamsHandler;
import com.aizuda.snailjob.client.job.core.util.ValidatorUtils;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;


public abstract class UpdateHandler<H> extends AbstractParamsHandler<H, Boolean> {

    public UpdateHandler(JobTaskTypeEnum typeEnum, Long jobId) {
        super(typeEnum);
        // 更新必须要id
        setId(jobId);
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {
        if (getReqDTO().getTriggerType() == TriggerTypeEnum.WORK_FLOW.getType()) {
            // 工作流没有调度时间
            setTriggerInterval("*");
        }
    }

    @Override
    protected Boolean doExecute() {
        Result<Object> result = client.updateJob(getReqDTO());
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return (Boolean) result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(Update.class, getReqDTO());
    }

}
