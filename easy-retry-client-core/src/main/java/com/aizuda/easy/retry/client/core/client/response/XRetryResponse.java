package com.aizuda.easy.retry.client.core.client.response;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.XRetryRequest;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * @author www.byteblogs.com
 * @date 2022-03-08
 * @since 2.0
 */
@Slf4j
public class XRetryResponse {

    private static final ConcurrentMap<String, Consumer<NettyResult>> responsePool = new ConcurrentHashMap<>();

    public XRetryResponse(XRetryRequest retryRequest, Consumer<NettyResult> callable) {
        responsePool.put(retryRequest.getRequestId(), callable);
    }

    public static void cache(XRetryRequest retryRequest, Consumer<NettyResult> callable) {
        responsePool.put(retryRequest.getRequestId(), callable);
    }

    public static void invoke(String requestId, NettyResult nettyResult) {
        Consumer<NettyResult> resultConsumer = responsePool.get(requestId);
        if (Objects.isNull(resultConsumer)) {
            LogUtils.info(log, "未发现 requestId:[{}] 的回调对象", requestId);
            return;
        }

        try {
            resultConsumer.accept(nettyResult);
        } catch (Exception e) {
            LogUtils.error(log, "回调处理失败 requestId:[{}]",requestId, e );
        } finally {
            responsePool.remove(requestId);
        }

    }

}
