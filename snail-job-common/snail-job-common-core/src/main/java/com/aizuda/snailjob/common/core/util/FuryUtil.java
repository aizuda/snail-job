package com.aizuda.snailjob.common.core.util;

import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;

import java.nio.charset.StandardCharsets;
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
        return new String(bytes);
    }


    public static <T> T deserialize(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        //noinspection unchecked
        return (T) SERIALIZER.deserialize(content.getBytes(StandardCharsets.UTF_8));
    }
}
