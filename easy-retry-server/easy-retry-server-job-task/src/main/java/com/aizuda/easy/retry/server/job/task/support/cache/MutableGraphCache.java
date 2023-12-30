package com.aizuda.easy.retry.server.job.task.support.cache;

import com.aizuda.easy.retry.server.common.util.GraphUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.graph.MutableGraph;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaowoniu
 * @date 2023-12-30 13:18:07
 * @since 2.6.0
 */
public class MutableGraphCache {

    private static final Cache<Long/*工作流批次*/, MutableGraph<Long>> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(8) // 并发级别
                .expireAfterWrite(5, TimeUnit.MINUTES) // 写入后的过期时间
                .build();
    }

    /**
     * 获取指定workflowBatchId的可变图对象，若图对象不存在则使用给定的jsonGraph反序列化生成新的图对象并返回
     *
     * @param workflowBatchId 工作流批次ID
     * @param jsonGraph       JSON格式的图对象字符串
     * @return {@link MutableGraph} 图对象
     */
    public static MutableGraph<Long> getOrDefault(Long workflowBatchId, String jsonGraph) {
        return Optional.ofNullable(cache.getIfPresent(workflowBatchId)).orElse(GraphUtils.deserializeJsonToGraph(jsonGraph));
    }

    /**
     * 根据给定的workflowBatchId获取图对象。
     *
     * @param workflowBatchId 工作流批次ID
     * @return {@link MutableGraph} 返回对应的图对象，如果不存在则返回空图
     */
    public static MutableGraph<Long> get(Long workflowBatchId) {
        return getOrDefault(workflowBatchId, "");
    }


}
