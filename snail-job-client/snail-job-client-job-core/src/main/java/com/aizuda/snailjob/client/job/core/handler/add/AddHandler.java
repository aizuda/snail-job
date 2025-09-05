package com.aizuda.snailjob.client.job.core.handler.add;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.job.core.handler.AbstractParamsHandler;
import com.aizuda.snailjob.client.common.util.ValidatorUtils;
import com.aizuda.snailjob.model.validate.group.Add;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.model.Result;

import static com.aizuda.snailjob.client.job.core.enums.TriggerTypeEnum.WORK_FLOW;

public abstract class AddHandler<H> extends AbstractParamsHandler<H, Long> {

    public AddHandler(JobTaskTypeEnum taskType) {
        super(taskType);
    }

    @Override
    protected Long doExecute() {
        Result<Long> result = clientV2.addJob(getReqDTO());
        return result.getData();
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
