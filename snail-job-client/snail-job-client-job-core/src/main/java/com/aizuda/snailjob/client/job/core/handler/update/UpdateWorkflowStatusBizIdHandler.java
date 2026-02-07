package com.aizuda.snailjob.client.job.core.handler.update;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.job.core.handler.AbstractJobRequestHandler;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.model.request.WorkflowStatusUpdateBizIdRequest;


/**
 * Handler for updating workflow status by bizId.
 *
 * @author opensnail
 * @since sj_1.10.0
 */
public class UpdateWorkflowStatusBizIdHandler extends AbstractJobRequestHandler<Boolean> {
    private final WorkflowStatusUpdateBizIdRequest statusDTO;

    public UpdateWorkflowStatusBizIdHandler(String bizId) {
        this.statusDTO = new WorkflowStatusUpdateBizIdRequest();
        statusDTO.setBizId(bizId);
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        Result<Boolean> result = clientV2.updateWorkflowStatusByBizId(statusDTO);
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(statusDTO.getBizId() != null && !statusDTO.getBizId().isEmpty()
                && statusDTO.getStatus() != null, "bizId cannot be null or empty and status cannot be null");
    }

    /**
     * Set workflow status
     *
     * @param status the status to set
     * @return this handler
     */
    public UpdateWorkflowStatusBizIdHandler setStatus(Integer status) {
        statusDTO.setStatus(status);
        return this;
    }
}