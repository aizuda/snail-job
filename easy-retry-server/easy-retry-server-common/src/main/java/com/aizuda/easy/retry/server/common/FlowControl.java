package com.aizuda.easy.retry.server.common;
import com.google.common.cache.Cache;
import com.google.common.util.concurrent.RateLimiter;

/**
 * 流量控制
 * @author: zuoJunLin
 * @date : 2023-11-21 13:04
 * @since 2.5.0
 */
public interface FlowControl {

    RateLimiter getRateLimiter(Cache<String, RateLimiter> rateLimiterCache, String key, double rateLimiterThreshold);
}


