package com.x.retry.server.support.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.util.concurrent.RateLimiter;
import com.x.retry.common.core.context.SpringContext;
import com.x.retry.common.core.log.LogUtils;
import com.x.retry.server.persistence.mybatis.mapper.ServerNodeMapper;
import com.x.retry.server.persistence.mybatis.po.RetryTask;
import com.x.retry.server.persistence.mybatis.po.ServerNode;
import com.x.retry.server.support.FilterStrategy;
import com.x.retry.server.support.IdempotentStrategy;
import com.x.retry.server.support.RetryContext;
import com.x.retry.server.support.cache.CacheGroupRateLimiter;
import com.x.retry.server.support.context.MaxAttemptsPersistenceRetryContext;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 生成 {@link FilterStrategy} 实例.
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-30 10:03
 */
public class FilterStrategies {

    private FilterStrategies() {
    }

    /**
     * 延迟等级的过滤策略
     *
     * @return {@link DelayLevelFilterStrategies} 延迟等级的过滤策略
     */
    public static FilterStrategy delayLevelFilter() {
        return new DelayLevelFilterStrategies();
    }

    /**
     * 使用BitSet幂等的过滤策略
     *
     * @return {@link BitSetIdempotentFilterStrategies} BitSet幂等的过滤策略
     */
    public static FilterStrategy bitSetIdempotentFilter(IdempotentStrategy<String, Integer> idempotentStrategy) {
        return new BitSetIdempotentFilterStrategies(idempotentStrategy);
    }

    /**
     * 场景黑名单策略
     *
     * @return {@link SceneBlackFilterStrategies} 场景黑名单策略
     */
    public static FilterStrategy sceneBlackFilter() {
        return new SceneBlackFilterStrategies();
    }

    /**
     * 检查是否存在存活的客户端POD
     *
     * @return {@link CheckAliveClientPodFilterStrategies} 客户端存活POD检查策略
     */
    public static FilterStrategy checkAliveClientPodFilter() {
        return new CheckAliveClientPodFilterStrategies();
    }

    /**
     * 检查分配的客户端是否达到限流阈值
     *
     * @return {@link RateLimiterFilterStrategies} 检查分配的客户端是否达到限流阈值
     */
    public static FilterStrategy rateLimiterFilter() {
        return new RateLimiterFilterStrategies();
    }


    /**
     * 延迟等级的过滤策略
     * <p>
     * 根据延迟等级的时间计算下次触发时间是否小于当前时间，满足则返回true 否则返回false
     */
    private static final class DelayLevelFilterStrategies implements FilterStrategy {

        @Override
        public boolean filter(RetryContext retryContext) {
            MaxAttemptsPersistenceRetryContext context = (MaxAttemptsPersistenceRetryContext) retryContext;

            LocalDateTime nextTriggerAt = context.getRetryTask().getNextTriggerAt();
            return nextTriggerAt.isBefore(LocalDateTime.now());
        }

        @Override
        public int order() {
            return 0;
        }
    }

    /**
     * 使用BitSet幂等的过滤策略
     * <p>
     * 判断BitSet中是否存在，若存在则放回false 否则返回true
     */
    private static final class BitSetIdempotentFilterStrategies implements FilterStrategy {

        private IdempotentStrategy<String, Integer> idempotentStrategy;

        public BitSetIdempotentFilterStrategies(IdempotentStrategy<String, Integer> idempotentStrategy) {
            this.idempotentStrategy = idempotentStrategy;
        }

        @Override
        public boolean filter(RetryContext retryContext) {
            RetryTask retryTask = retryContext.getRetryTask();
            return !idempotentStrategy.isExist(retryTask.getGroupName(), retryTask.getId().intValue());
        }

        @Override
        public int order() {
            return 1;
        }
    }

    /**
     * 场景黑名单策略
     * <p>
     * 如果重试的数据在黑名单中的则返回false 否则为true
     */
    private static final class SceneBlackFilterStrategies implements FilterStrategy {

        @Override
        public boolean filter(RetryContext retryContext) {
            MaxAttemptsPersistenceRetryContext context = (MaxAttemptsPersistenceRetryContext) retryContext;
            return !context.getSceneBlacklist().contains(retryContext.getRetryTask().getSceneName());
        }

        @Override
        public int order() {
            return 2;
        }
    }

    /**
     * 检查是否存在存活的客户端POD
     */
    private static final class CheckAliveClientPodFilterStrategies implements FilterStrategy {

        @Override
        public boolean filter(RetryContext retryContext) {
            MaxAttemptsPersistenceRetryContext context = (MaxAttemptsPersistenceRetryContext) retryContext;
            ServerNode serverNode = context.getServerNode();

            if (Objects.isNull(serverNode)) {
                return false;
            }

            ServerNodeMapper serverNodeMapper = SpringContext.getBeanByType(ServerNodeMapper.class);

            return 1 == serverNodeMapper.selectCount(new LambdaQueryWrapper<ServerNode>().eq(ServerNode::getHostId, serverNode.getHostId()));
        }

        @Override
        public int order() {
            return 3;
        }
    }

    /**
     * 检查是否存在存活的客户端POD
     */
    private static final class RateLimiterFilterStrategies implements FilterStrategy {

        @Override
        public boolean filter(RetryContext retryContext) {
            MaxAttemptsPersistenceRetryContext context = (MaxAttemptsPersistenceRetryContext) retryContext;
            ServerNode serverNode = context.getServerNode();

            RateLimiter rateLimiter = CacheGroupRateLimiter.getRateLimiterByKey(serverNode.getHostId());
            if (!rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
                LogUtils.error("该POD:[{}]已到达最大限流阈值，本次重试不执行", serverNode.getHostId());
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        }

        @Override
        public int order() {
            return 4;
        }
    }

}
