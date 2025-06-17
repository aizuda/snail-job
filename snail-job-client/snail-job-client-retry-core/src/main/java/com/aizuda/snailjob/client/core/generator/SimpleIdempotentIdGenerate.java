package com.aizuda.snailjob.client.core.generator;

import com.aizuda.snailjob.client.core.IdempotentIdGenerate;
import com.aizuda.snailjob.common.core.model.IdempotentIdContext;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;

/**
 * 默认的idempotentId 生成器
 *
 * @author: opensnail
 * @date : 2022-03-08 09:42
 */
public class SimpleIdempotentIdGenerate implements IdempotentIdGenerate {

    @Override
    public String idGenerate(IdempotentIdContext context) throws Exception {
        if (Objects.isNull(context)) {
            return UUID.randomUUID().toString();
        }

        String id = MessageFormat.format("{0}_{1}_{2}_{3}", context.getScene(), context.getTargetClassName(),
                context.getMethodName(), context.getSerializeArgs());
        return Hashing.md5().hashBytes(id.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
