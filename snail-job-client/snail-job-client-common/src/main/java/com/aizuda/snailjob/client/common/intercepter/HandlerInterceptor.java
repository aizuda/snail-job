package com.aizuda.snailjob.client.common.intercepter;

import com.aizuda.snailjob.client.common.rpc.supports.scan.EndPointInfo;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpRequest;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpResponse;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpRequest;
import com.aizuda.snailjob.client.common.rpc.supports.http.HttpResponse;
import com.aizuda.snailjob.client.common.rpc.supports.scan.EndPointInfo;

/**
 * @author: opensnail
 * @date : 2024-04-12
 * @since : 3.3.0
 */
public interface HandlerInterceptor {

    boolean preHandle(HttpRequest httpRequest, HttpResponse httpResponse, EndPointInfo handler);

    void postHandle(HttpRequest httpRequest, HttpResponse httpResponse, EndPointInfo handler);

    void afterCompletion(HttpRequest httpRequest, HttpResponse httpResponse, EndPointInfo handler, Exception ex);

    int order();
}
