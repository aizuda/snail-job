package com.aizuda.snailjob.client.common.rpc.supports.handler;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.config.SnailJobProperties.ThreadPoolConfig;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpRequest;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpResponse;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcSnailJobRequest;
import com.aizuda.snailjob.common.core.grpc.auto.Metadata;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.google.protobuf.Any;
import com.google.protobuf.UnsafeByteOperations;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: opensnail
 * @date : 2024-08-22
 */
public class UnaryRequestHandler implements ServerCalls.UnaryMethod<GrpcSnailJobRequest, GrpcResult> {


    private final ThreadPoolExecutor dispatcherThreadPool;
    private final SnailDispatcherRequestHandler dispatcher;

    public UnaryRequestHandler(final ThreadPoolConfig dispatcherThreadPool,
        final SnailDispatcherRequestHandler handler) {
        this.dispatcher = handler;
        this.dispatcherThreadPool = new ThreadPoolExecutor(
            dispatcherThreadPool.getCorePoolSize(), dispatcherThreadPool.getMaximumPoolSize(),
            dispatcherThreadPool.getKeepAliveTime(),
            dispatcherThreadPool.getTimeUnit(), new LinkedBlockingQueue<>(dispatcherThreadPool.getQueueCapacity()),
            new CustomizableThreadFactory("snail-grpc-server-"));
    }

    @Override
    public void invoke(final GrpcSnailJobRequest snailJobRequest, final StreamObserver<GrpcResult> streamObserver) {

        Metadata metadata = snailJobRequest.getMetadata();

        GrpcRequest grpcRequest = GrpcRequest.builder()
            .httpRequest(new HttpRequest(metadata.getHeadersMap(), metadata.getUri()))
            .httpResponse(new HttpResponse())
            .snailJobRequest(snailJobRequest)
            .build();

        // 执行任务
        dispatcherThreadPool.execute(() -> {
            SnailJobRpcResult snailJobRpcResult = null;
            try {
                snailJobRpcResult = dispatcher.dispatch(grpcRequest);
            } catch (Exception e) {
                snailJobRpcResult = new SnailJobRpcResult(StatusEnum.NO.getStatus(), e.getMessage(), null, 0);
            } finally {
                GrpcResult grpcResult = GrpcResult.newBuilder()
                    .setStatus(snailJobRpcResult.getStatus())
                    .setMessage(Optional.ofNullable(snailJobRpcResult.getMessage()).orElse(StrUtil.EMPTY))
                    .setData(Any.newBuilder()
                        .setValue(UnsafeByteOperations.unsafeWrap(
                            JsonUtil.toJsonString(snailJobRpcResult.getData()).getBytes()))
                        .build())
                    .build();

                streamObserver.onNext(grpcResult);
                streamObserver.onCompleted();
            }
        });

    }
}
