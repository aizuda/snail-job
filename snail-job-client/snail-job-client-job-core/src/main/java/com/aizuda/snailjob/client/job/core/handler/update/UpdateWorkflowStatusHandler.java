package com.aizuda.snailjob.client.job.core.handler.update;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.handler.AbstractJobRequestHandler;
import com.aizuda.snailjob.client.common.util.ValidatorUtils;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.model.request.base.StatusUpdateRequest;


public class UpdateWorkflowStatusHandler extends AbstractJobRequestHandler<Boolean> {
    private final StatusUpdateRequest statusDTO;

    public UpdateWorkflowStatusHandler(Long id) {
        this.statusDTO = new StatusUpdateRequest();
        setId(id);
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        Result<Object> result = clientV2.updateWorkFlowStatus(statusDTO);
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return (Boolean) result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(statusDTO);
    }

    /**
     * 设置任务/工作流ID
     *
     * @param id
     * @return
     */
    private UpdateWorkflowStatusHandler setId(Long id) {
        this.statusDTO.setId(id);
        return this;
    }

    /**
     * 设置状态
     *
     * @param status
     * @return
     */
    public UpdateWorkflowStatusHandler setStatus(StatusEnum status) {
        this.statusDTO.setJobStatus(status.getStatus());
        this.statusDTO.setStatus(status.getStatus());
        return this;
    }
}
