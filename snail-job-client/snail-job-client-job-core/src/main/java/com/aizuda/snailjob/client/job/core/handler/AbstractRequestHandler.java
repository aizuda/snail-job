package com.aizuda.snailjob.client.job.core.handler;

import com.aizuda.snailjob.client.common.exception.SnailJobClientException;

/**
 * @author opensnail
 * @date 2024-09-29 20:40:10
 * @since sj_1.1.0
 */
public abstract class AbstractRequestHandler<R> implements RequestHandler<R> {

    /**
     * 具体调用
     * @return
     */
    @Override
    public R execute() {
        if (checkRequest()) {
            return doExecute();
        } else {
            throw new SnailJobClientException("snail job openapi check error");
        }
    }

    protected abstract R doExecute();

    protected abstract boolean checkRequest();
}
