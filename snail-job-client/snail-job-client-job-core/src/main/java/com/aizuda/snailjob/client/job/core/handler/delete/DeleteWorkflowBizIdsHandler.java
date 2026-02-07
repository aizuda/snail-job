package com.aizuda.snailjob.client.job.core.handler.delete;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.job.core.handler.AbstractJobRequestHandler;
import com.aizuda.snailjob.common.core.model.Result;

import java.util.Set;

/**
 * Handler for deleting workflows by bizIds.
 *
 * @author opensnail
 * @since sj_1.10.0
 */
public class DeleteWorkflowBizIdsHandler extends AbstractJobRequestHandler<Boolean> {
    private final Set<String> bizIds;

    public DeleteWorkflowBizIdsHandler(Set<String> bizIds) {
        this.bizIds = bizIds;
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        Result<Boolean> result = clientV2.deleteWorkflowByBizIds(bizIds);
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(bizIds != null && !bizIds.isEmpty()
                && !bizIds.contains(null) && !bizIds.contains(""), "bizIds cannot be null or empty");
    }
}