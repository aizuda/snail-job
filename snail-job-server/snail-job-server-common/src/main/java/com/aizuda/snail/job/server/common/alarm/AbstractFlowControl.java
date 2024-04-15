package com.aizuda.snail.job.server.common.alarm;

import com.aizuda.snail.job.server.common.cache.CacheNotifyRateLimiter;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.Objects;


/**
 * @author: zuoJunLin
 * @date : 2023-11-21 13:04
 * @since 2.5.0
 */
@Slf4j
public abstract class AbstractFlowControl<E extends ApplicationEvent> implements ApplicationListener<E> {

    protected RateLimiter getRateLimiter(String key, double rateLimiterThreshold) {
        RateLimiter rateLimiter = CacheNotifyRateLimiter.getRateLimiterByKey(key);
        if (Objects.isNull(rateLimiter) || rateLimiter.getRate() != rateLimiterThreshold) {
            CacheNotifyRateLimiter.put(key, RateLimiter.create(rateLimiterThreshold));
        }

        return rateLimiter;
    }
}
