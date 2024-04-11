package com.aizuda.easy.retry.client.common.netty.server;

import cn.hutool.core.lang.Pair;
import com.aizuda.easy.retry.client.common.netty.RequestMethod;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author opensnail
 * @date 2024-04-11 22:58:21
 * @since 3.3.0
 */
public final class EndPointInfoCache {

    private EndPointInfoCache() {}

    private static final ConcurrentHashMap<Pair<String, RequestMethod>, EndPointInfo> ENDPOINT_REPOSITORY = new ConcurrentHashMap<>();

    public static void put(EndPointInfo endPointInfo) {
        ENDPOINT_REPOSITORY.put(Pair.of(endPointInfo.getPath(), endPointInfo.getRequestMethod()), endPointInfo);
    }

    public static EndPointInfo get(String path, RequestMethod method) {
        return ENDPOINT_REPOSITORY.get(Pair.of(path, method));
    }

    public static boolean isExisted(String path, RequestMethod method) {
        return Objects.nonNull(ENDPOINT_REPOSITORY.get(Pair.of(path, method)));
    }

}
