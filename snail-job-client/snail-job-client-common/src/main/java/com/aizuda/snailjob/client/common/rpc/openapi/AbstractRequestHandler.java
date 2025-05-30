package com.aizuda.snailjob.client.common.rpc.openapi;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;

/**
 * @author opensnail
 * @date 2024-09-29 20:40:10
 * @since sj_1.1.0
 */
public abstract class AbstractRequestHandler<R> implements RequestHandler<R> {

    /**
     * 具体调用
     *
     * @return
     */
    @Override
    public R execute() {
        Pair<Boolean, String> checked = checkRequest();
        if (checked.getKey()) {
            beforeExecute();
            R r = doExecute();
            afterExecute(r);
            return r;
        } else {
            throw new SnailJobClientException("snail job openapi check error. [{}]", checked.getValue());
        }
    }

    protected void afterExecute(R r) {
    }

    protected void beforeExecute() {
    }

    protected abstract R doExecute();

    protected abstract Pair<Boolean, String> checkRequest();
}
