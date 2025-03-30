package com.aizuda.snailjob.client.common.rpc.openapi;

public interface RequestHandler<R> {

    R execute();

}