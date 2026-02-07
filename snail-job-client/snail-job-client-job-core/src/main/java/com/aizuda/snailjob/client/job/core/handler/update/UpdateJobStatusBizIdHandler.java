package com.aizuda.snailjob.client.job.core.handler.update;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.job.core.handler.AbstractJobRequestHandler;
import com.aizuda.snailjob.client.common.util.ValidatorUtils;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.model.request.StatusUpdateBizIdRequest;

/**
 * Update job status handler by bizId.
 *
 * @author opensnail
 * @since sj_1.2.0
 */
public class UpdateJobStatusBizIdHandler extends AbstractJobRequestHandler<Boolean> {

    private final StatusUpdateBizIdRequest statusDTO;

    public UpdateJobStatusBizIdHandler(String bizId) {
        this.statusDTO = new StatusUpdateBizIdRequest();
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
        Result<Boolean> result = clientV2.updateJobStatusByBizId(statusDTO);
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(statusDTO);
    }

    /**
     * 设置状态
     *
     * @param status 状态枚举
     * @return this
     */
    public UpdateJobStatusBizIdHandler setStatus(StatusEnum status) {
        this.statusDTO.setStatus(status.getStatus());
        return this;
    }

}