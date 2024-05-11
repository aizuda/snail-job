package com.aizuda.snailjob.common.core.rpc;

import com.aizuda.snailjob.common.core.model.Result;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author: opensanil
 * @date : 2024-05-11
 * @since : sj_1.0.0
 */
@AllArgsConstructor
@Getter
public class SnailJobFuture<R extends Result<Object>> extends CompletableFuture<R> {
    private final Long requestId;
    private final long timeout;
    private final TimeUnit unit;

    public static <R extends Result<Object>> SnailJobFuture<R> newFuture(Long requestId, long timeout, TimeUnit unit) {
        return new SnailJobFuture<>(requestId, timeout, unit);
    }

}
