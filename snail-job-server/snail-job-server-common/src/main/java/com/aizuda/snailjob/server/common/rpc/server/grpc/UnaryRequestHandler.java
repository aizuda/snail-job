package com.aizuda.snailjob.server.common.rpc.server.grpc;

import  org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.SnailJobGrpcRequest;
import com.aizuda.snailjob.common.core.grpc.auto.Metadata;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.dto.GrpcRequest;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;

/**
 * @author: opensnail
 * @date : 2024-08-22
 */
public class UnaryRequestHandler implements ServerCalls.UnaryMethod<SnailJobGrpcRequest, GrpcResult>{

    @Override
    public void invoke(final SnailJobGrpcRequest snailJobRequest, final StreamObserver<GrpcResult> streamObserver) {
        Metadata metadata = snailJobRequest.getMetadata();
        GrpcRequest grpcRequest = GrpcRequest.builder()
            .uri(metadata.getUri())
            .snailJobRequest(snailJobRequest)
            .streamObserver(streamObserver)
            .build();

        ActorRef actorRef = ActorGenerator.requestGrpcHandlerActor();
        actorRef.tell(grpcRequest, actorRef);
    }
}
