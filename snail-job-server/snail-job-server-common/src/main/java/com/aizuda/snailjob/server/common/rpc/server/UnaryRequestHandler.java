package com.aizuda.snailjob.server.common.rpc.server;

import akka.actor.ActorRef;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcSnailJobRequest;
import com.aizuda.snailjob.common.core.grpc.auto.Metadata;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.dto.GrpcRequest;
import com.aizuda.snailjob.server.common.dto.NettyHttpRequest;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

/**
 * @author: opensnail
 * @date : 2024-08-22
 */
public class UnaryRequestHandler implements ServerCalls.UnaryMethod<GrpcSnailJobRequest, GrpcResult>{

    @Override
    public void invoke(final GrpcSnailJobRequest snailJobRequest, final StreamObserver<GrpcResult> streamObserver) {
        // 处理请求并返回响应
//        GrpcResult result = GrpcResult.newBuilder().setMessage("调度成功").setStatus(1).build();

        Metadata metadata = snailJobRequest.getMetadata();
        GrpcRequest grpcRequest = GrpcRequest.builder()
            .uri(metadata.getUri())
            .snailJobRequest(snailJobRequest)
            .streamObserver(streamObserver)
            .build();

        ActorRef actorRef = ActorGenerator.requestHandlerActor();
        actorRef.tell(grpcRequest, actorRef);

//        // 发送响应
//        streamObserver.onNext(result);
//        streamObserver.onCompleted();
    }
}
