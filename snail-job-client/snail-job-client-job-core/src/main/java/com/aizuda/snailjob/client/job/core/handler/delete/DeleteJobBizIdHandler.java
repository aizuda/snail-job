package com.aizuda.snailjob.client.job.core.handler.delete;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.job.core.handler.AbstractJobRequestHandler;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.model.request.DeleteBizIdRequest;

import java.util.Set;

/**
 * Delete job handler by bizIds.
 *
 * @author opensnail
 * @since sj_1.2.0
 */
public class DeleteJobBizIdHandler extends AbstractJobRequestHandler<Boolean> {

    private final DeleteBizIdRequest deleteDTO;

    public DeleteJobBizIdHandler(Set<String> bizIds) {
        this.deleteDTO = new DeleteBizIdRequest();
        this.deleteDTO.setBizIds(bizIds);
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        Result<Boolean> result = clientV2.deleteJobByBizIds(deleteDTO);
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(deleteDTO.getBizIds() != null && !deleteDTO.getBizIds().isEmpty()
                && !deleteDTO.getBizIds().contains(""), "bizIds cannot be null or empty");
    }

}