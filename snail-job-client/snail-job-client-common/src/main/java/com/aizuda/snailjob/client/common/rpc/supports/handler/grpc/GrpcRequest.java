package com.aizuda.snailjob.client.common.rpc.supports.handler.grpc;


import com.aizuda.snailjob.client.common.rpc.supports.http.HttpRequest;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpResponse;
import com.aizuda.snailjob.common.core.grpc.auto.SnailJobGrpcRequest;
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

    private SnailJobGrpcRequest snailJobRequest;
    private final HttpResponse httpResponse;
    private final HttpRequest httpRequest;

}
