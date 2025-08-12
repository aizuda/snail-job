package com.aizuda.snailjob.common.core.util;

import com.aizuda.snailjob.common.core.config.ForyProperties;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.github.luben.zstd.Zstd;
import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.CompatibleMode;
import org.apache.fory.config.Language;
import org.apache.fory.resolver.AllowListChecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ForyUtil {
    /**
     * 默认最大解压缩大小
     */
    private static final int DEFAULT_MAX_DECOMPRESSED_SIZE = 16384;

    private static final String FORY_BLACK_LIST = "foryBlackList.txt";

    private static final ThreadSafeFory SERIALIZER;

    static {
        // fix => https://gitee.com/aizuda/snail-job/issues/ICQV61
        AllowListChecker checker = new AllowListChecker(AllowListChecker.CheckLevel.STRICT);

        ThreadSafeFory fory = Fory.builder()
                .withLanguage(Language.JAVA)
                .requireClassRegistration(true)
                .withCompatibleMode(CompatibleMode.COMPATIBLE)
                .buildThreadSafeForyPool(
                        Runtime.getRuntime().availableProcessors(),
                        Runtime.getRuntime().availableProcessors() * 2,
                        30,
                        TimeUnit.MINUTES
                );
        fory.setClassChecker(checker);

        Set<String> disableClasses = getDisallowClasses();
        checker.disallowClasses(disableClasses);

        SERIALIZER = fory;
    }

    private static Set<String> getDisallowClasses() {
        // 读取黑名单配置
        Set<String> disallowClasses = new HashSet<>();
        // 使用类加载器读取resources目录下的配置文件
        try (InputStream is = ForyUtil.class.getClassLoader().getResourceAsStream(FORY_BLACK_LIST);
             BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // 跳过空行和注释行（假设以#开头的是注释）
                if (!line.isEmpty() && !line.startsWith("#")) {
                    disallowClasses.add(line);
                }
            }
        } catch (NullPointerException e) {
            SnailJobLog.LOCAL.error("foryBlackList.txt file not found in resources", e);
        } catch (IOException e) {
            SnailJobLog.LOCAL.error("Failed to read foryBlackList.txt", e);
        }
        return disallowClasses;
    }


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
        } catch (Exception e) {
            SnailJobLog.LOCAL.warn("Get ForyProperties failed.", e);
        }
        int decompressedSize = Objects.nonNull(properties) ? properties.getDecompressedSize() : DEFAULT_MAX_DECOMPRESSED_SIZE;

        byte[] bytes = Base64.getDecoder().decode(content);
        int size = (int) Zstd.decompressedSize(bytes);
        if (size > decompressedSize) {
            throw new SnailJobCommonException("Decompressed size exceeds the allowed limit.");
        }
        bytes = Zstd.decompress(bytes, size);
        //noinspection unchecked
        return (T) SERIALIZER.deserialize(bytes);
    }
}
