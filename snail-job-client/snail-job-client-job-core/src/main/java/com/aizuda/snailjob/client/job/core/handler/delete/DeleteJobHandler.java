package com.aizuda.snailjob.client.job.core.handler.delete;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.handler.AbstractJobRequestHandler;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;

import java.util.Set;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.client.job.core.handler.delete
 * @Project：snail-job
 * @Date：2024/11/21 22:38
 * @Filename：DeleteJobHandler
 */
public class DeleteJobHandler extends AbstractJobRequestHandler<Boolean> {
    private final Set<Long> toDeleteIds;

    public DeleteJobHandler(Set<Long> toDeleteIds) {
        this.toDeleteIds = toDeleteIds;
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        Result<Object> result = client.deleteJob(toDeleteIds);
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return (Boolean)result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(toDeleteIds != null && !toDeleteIds.isEmpty() && !toDeleteIds.contains(0L),  "toDeleteId不能为null或0");
    }
}
