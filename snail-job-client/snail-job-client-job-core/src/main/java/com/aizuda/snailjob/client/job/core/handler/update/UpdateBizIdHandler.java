package com.aizuda.snailjob.client.job.core.handler.update;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.job.core.enums.TriggerTypeEnum;
import com.aizuda.snailjob.client.job.core.handler.AbstractParamsBizIdHandler;
import com.aizuda.snailjob.client.common.util.ValidatorUtils;
import com.aizuda.snailjob.model.validate.group.Update;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.model.Result;

import java.util.Objects;


public abstract class UpdateBizIdHandler<H> extends AbstractParamsBizIdHandler<H, Boolean> {

    public UpdateBizIdHandler(String bizId, JobTaskTypeEnum typeEnum) {
        super(bizId, typeEnum);
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {
        Integer triggerType = getReqDTO().getTriggerType();
        if (Objects.nonNull(triggerType) && triggerType == TriggerTypeEnum.WORK_FLOW.getType()) {
            setTriggerInterval("*");
        }
    }

    @Override
    protected Boolean doExecute() {
        Result<Boolean> result = clientV2.updateJobByBizId(getReqDTO());
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(Update.class, getReqDTO());
    }

}