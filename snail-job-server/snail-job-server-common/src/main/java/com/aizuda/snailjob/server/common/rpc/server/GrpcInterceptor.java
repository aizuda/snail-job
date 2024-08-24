package com.aizuda.snailjob.server.common.rpc.server;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: opensnail
 * @date : 2024-08-22
 */
@Slf4j
public class GrpcInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> serverCall, final Metadata metadata,
        final ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String fullMethodName = serverCall.getMethodDescriptor().getFullMethodName();
        long start = System.currentTimeMillis();
        Context context = Context.current();

        try {
            return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
        } finally {
            log.debug("method invoked: {} cast:{}ms", fullMethodName, System.currentTimeMillis() - start);
        }
    }

}
