package com.aizuda.snailjob.client.common.rpc.supports.handler;


import com.aizuda.snailjob.client.common.rpc.supports.http.HttpRequest;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpResponse;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcSnailJobRequest;
import io.grpc.stub.StreamObserver;
import lombok.Builder;
import lombok.Data;

/**
 * netty客户端请求模型
 *
 * @author: opensnail
 * @date : 2023-07-24 09:32
 */
@Data
@Builder
public class GrpcRequest {

    private GrpcSnailJobRequest snailJobRequest;
    private final HttpResponse httpResponse;
    private final HttpRequest httpRequest;

}
