package com.aizuda.snailjob.client.common.rpc.supports.handler;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpRequest;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpResponse;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcSnailJobRequest;
import com.aizuda.snailjob.common.core.grpc.auto.Metadata;
import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.google.protobuf.Any;
import com.google.protobuf.UnsafeByteOperations;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import io.netty.handler.codec.http.HttpUtil;

import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: opensnail
 * @date : 2024-08-22
 */
public class UnaryRequestHandler implements ServerCalls.UnaryMethod<GrpcSnailJobRequest, GrpcResult>{


    private final ThreadPoolExecutor threadPoolExecutor;
    private final SnailDispatcherRequestHandler dispatcher;
    public UnaryRequestHandler(final ThreadPoolExecutor dispatcherThreadPool,
        final SnailDispatcherRequestHandler handler) {
        this.threadPoolExecutor = dispatcherThreadPool;
        this.dispatcher = handler;
    }

    @Override
    public void invoke(final GrpcSnailJobRequest snailJobRequest, final StreamObserver<GrpcResult> streamObserver) {

        Metadata metadata = snailJobRequest.getMetadata();

        GrpcRequest grpcRequest = GrpcRequest.builder()
            .httpRequest(new HttpRequest( metadata.getHeadersMap(), metadata.getUri()))
            .httpResponse(new HttpResponse())
            .snailJobRequest(snailJobRequest)
            .build();

        // 执行任务
        threadPoolExecutor.execute(() -> {
            NettyResult nettyResult = null;
            try {
                nettyResult = dispatcher.dispatch(grpcRequest);
            } catch (Exception e) {
                nettyResult = new NettyResult(StatusEnum.NO.getStatus(), e.getMessage(), null, 0);
            } finally {
                GrpcResult grpcResult = GrpcResult.newBuilder()
                    .setStatus(nettyResult.getStatus())
                    .setMessage(Optional.ofNullable(nettyResult.getMessage()).orElse(StrUtil.EMPTY))
                    .setData(Any.newBuilder()
                        .setValue(UnsafeByteOperations.unsafeWrap(JsonUtil.toJsonString(nettyResult.getData()).getBytes()))
                        .build())
                    .build();

                streamObserver.onNext(grpcResult);
                streamObserver.onCompleted();
            }
        });

    }
}
