package com.aizuda.easy.retry.server.common.cache;

import cn.hutool.core.lang.Pair;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.GroupConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaowoniu
 * @date 2024-03-29 23:15:26
 * @since 3.2.0
 */
public class CacheToken implements Lifecycle {

    private static Cache<Pair<String/*groupName*/, String/*namespaceId*/> , String/*Token*/> CACHE;

    public static void add(String groupName, String namespaceId,  String token) {
        CACHE.put(Pair.of(groupName, namespaceId), token);
    }

    public static String get(String groupName, String namespaceId) throws ExecutionException {
       return CACHE.get(Pair.of(groupName, namespaceId), () -> {
           AccessTemplate template = SpringContext.getBean(AccessTemplate.class);
           GroupConfig config = template.getGroupConfigAccess().getGroupConfigByGroupName(groupName, namespaceId);
           if (Objects.isNull(config)) {
              return SystemConstants.DEFAULT_TOKEN;
           }

           String token = config.getToken();
           add(groupName, namespaceId, token);
           return token;
       });
    }

    @Override
    public void start() {
        EasyRetryLog.LOCAL.info("CacheToken start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                // 若当前节点不在消费次组，则自动到期删除
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void close() {
        EasyRetryLog.LOCAL.info("CacheToken stop");
        CACHE.invalidateAll();
    }
}
