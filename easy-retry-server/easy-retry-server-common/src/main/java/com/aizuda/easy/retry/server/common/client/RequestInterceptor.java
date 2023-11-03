package com.aizuda.easy.retry.server.common.client;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author: byteblogs168
 * @date : 2023-11-03 17:40
 * @since : 2.4.0
 */
@Slf4j
public class RequestInterceptor implements Interceptor {

    public static final String TIMEOUT_TIME = "executorTimeout";

    @NotNull
    @Override
    public Response intercept(@NotNull final Chain chain) throws IOException {
        Request request = chain.request();
        String timeoutTime = request.header(TIMEOUT_TIME);
        if (StrUtil.isNotBlank(timeoutTime)) {
            int timeout = Integer.parseInt(timeoutTime);
            log.info("url:[{}] timeout:[{}]", request.url(), timeout);
            if (timeout <= 0) {
                return chain.proceed(request);
            }

            return chain
                .withReadTimeout(timeout, TimeUnit.SECONDS)
                .withConnectTimeout(timeout, TimeUnit.SECONDS)
                .withWriteTimeout(timeout, TimeUnit.SECONDS)
                .proceed(chain.request());
        }

        return chain.proceed(request);

    }
}
