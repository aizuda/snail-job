package com.aizuda.easy.retry.server.common.flow.control;
import com.aizuda.easy.retry.server.common.FlowControl;
import com.google.common.cache.Cache;
import com.google.common.util.concurrent.RateLimiter;
import java.util.Objects;


/**
 * @author: zuoJunLin
 * @date : 2023-11-21 13:04
 * @since 2.5.0
 */
public abstract class AbstractFlowControl implements FlowControl {

    public RateLimiter getRateLimiter(Cache<String, RateLimiter> rateLimiterCache, String key, double rateLimiterThreshold) {
        RateLimiter rateLimiter = rateLimiterCache.getIfPresent(key);
        if (Objects.isNull(rateLimiter)||rateLimiter.getRate()!=rateLimiterThreshold) {
            rateLimiterCache.put(key, RateLimiter.create(rateLimiterThreshold));
        }
        return rateLimiterCache.getIfPresent(key);
    }
}
