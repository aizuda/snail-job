package com.aizuda.easy.retry.server.retry.task.support.idempotent;

import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.server.common.IdempotentStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author www.byteblogs.com
 * @date 2023-10-19 21:54:57
 * @since 2.4.0
 */
@Slf4j
public class TimerIdempotent implements IdempotentStrategy<String, String> {

    private static final CopyOnWriteArraySet<String> cache = new CopyOnWriteArraySet<>();

    @Override
    public boolean set(String key, String value) {
        return cache.add(key.concat(StrUtil.UNDERLINE).concat(String.valueOf(value)));
    }

    @Override
    public String get(String s) {
       throw new UnsupportedOperationException("不支持此操作");
    }

    @Override
    public boolean isExist(String key, String value) {
        if (key == null || value == null) {
            log.error("异常监控. key:[{}] value:[{}]", key, value);
        }
        return cache.contains(key.concat(StrUtil.UNDERLINE).concat(String.valueOf(value)));
    }

    @Override
    public boolean clear(String key, String value) {
        return cache.removeIf(s-> s.equals(key.concat(StrUtil.UNDERLINE).concat(String.valueOf(value))));
    }
}
