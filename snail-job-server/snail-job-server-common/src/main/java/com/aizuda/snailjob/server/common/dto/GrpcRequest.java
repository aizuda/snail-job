package com.aizuda.snailjob.server.common.dto;


import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.SnailJobGrpcRequest;
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

    private SnailJobGrpcRequest snailJobRequest;

    private StreamObserver<GrpcResult> streamObserver;

    private String uri;

}
