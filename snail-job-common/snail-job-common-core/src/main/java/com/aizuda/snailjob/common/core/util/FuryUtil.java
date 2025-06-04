package com.aizuda.snailjob.common.core.util;

import com.github.luben.zstd.Zstd;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class FuryUtil {
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
        byte[] bytes = Base64.getDecoder().decode(content);
        bytes = Zstd.decompress(bytes, (int) Zstd.decompressedSize(bytes));
        //noinspection unchecked
        return (T) SERIALIZER.deserialize(bytes);
    }
}
