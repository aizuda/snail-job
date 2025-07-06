package com.aizuda.snailjob.server.common.cache;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.register.ServerRegister;
import com.aizuda.snailjob.server.common.triple.Pair;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaowoniu
 * @date 2024-03-29 23:15:26
 * @since 3.2.0
 */
@Component
public class CacheToken implements Lifecycle {

    private static Cache<Pair<String/*groupName*/, String/*namespaceId*/>, String/*Token*/> CACHE;

    public static void add(String groupName, String namespaceId, String token) {
        CACHE.put(Pair.of(groupName, namespaceId), token);
    }

    public static String get(String groupName, String namespaceId) {
        if (groupName.equals(ServerRegister.GROUP_NAME)){
            return getServerToken();
        }
        String token = CACHE.getIfPresent(Pair.of(groupName, namespaceId));
        if (StrUtil.isBlank(token)) {
            // 从DB获取数据
            AccessTemplate template = SnailSpringContext.getBean(AccessTemplate.class);
            GroupConfig config = template.getGroupConfigAccess().getGroupConfigByGroupName(groupName, namespaceId);
            if (Objects.isNull(config)) {
                return StrUtil.EMPTY;
            }

            token = config.getToken();
            add(groupName, namespaceId, token);
        }

        return token;
    }

    private static String getServerToken() {
        SystemProperties properties = SnailSpringContext.getBean(SystemProperties.class);
        return properties.getServerToken();
    }

    @Override
    public void start() {
        SnailJobLog.LOCAL.info("CacheToken start");
        CACHE = CacheBuilder.newBuilder()
                // 设置并发级别为cpu核心数
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                // 若当前节点不在消费次组，则自动到期删除
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void close() {
        SnailJobLog.LOCAL.info("CacheToken stop");
        CACHE.invalidateAll();
    }
}
