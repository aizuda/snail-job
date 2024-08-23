package com.aizuda.snailjob.client.common.rpc.server;

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

        try {
            return serverCallHandler.startCall(serverCall, metadata);
        } finally {
            log.info("method invoked: {} cast:{}ms", fullMethodName, System.currentTimeMillis() - start);
        }
    }

}
