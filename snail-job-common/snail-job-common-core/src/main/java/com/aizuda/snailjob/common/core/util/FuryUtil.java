package com.aizuda.snailjob.common.core.util;

import com.aizuda.snailjob.common.core.config.ForyProperties;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.github.luben.zstd.Zstd;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;

import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FuryUtil {
    /**
     * 默认最大解压缩大小
     */
    private static final int DEFAULT_MAX_DECOMPRESSED_SIZE = 16384;

    private static final ThreadSafeFury SERIALIZER = Fury.builder()
            .withLanguage(Language.JAVA)
            .requireClassRegistration(false)
            .withCompatibleMode(CompatibleMode.COMPATIBLE)
            .buildThreadSafeFuryPool(
                    Runtime.getRuntime().availableProcessors(),
                    Runtime.getRuntime().availableProcessors() * 2,
                    30,
                    TimeUnit.MINUTES
            );

    public static String serialize(Object object) {
        if (object == null) {
            return "";
        }

        byte[] bytes = SERIALIZER.serialize(object);
        bytes = Zstd.compress(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }


    public static <T> T deserialize(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        ForyProperties properties = null;
        try {
            properties = SnailSpringContext.getBean(ForyProperties.class);
        }catch (Exception e){
            SnailJobLog.LOCAL.warn("Get ForyProperties failed.", e);
        }
        int decompressedSize = Objects.nonNull(properties) ? properties.getDecompressedSize() : DEFAULT_MAX_DECOMPRESSED_SIZE;

        byte[] bytes = Base64.getDecoder().decode(content);
        int size = (int) Zstd.decompressedSize(bytes);
        if (size > decompressedSize){
            throw new SnailJobCommonException("Decompressed size exceeds the allowed limit.");
        }
        bytes = Zstd.decompress(bytes, (int) Zstd.decompressedSize(bytes));
        //noinspection unchecked
        return (T) SERIALIZER.deserialize(bytes);
    }
}
