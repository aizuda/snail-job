package com.aizuda.snailjob.client.core.generator;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.core.IdempotentIdGenerate;
import com.aizuda.snailjob.common.core.model.IdempotentIdContext;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

/**
 * 默认的idempotentId 生成器
 *
 * @author: opensnail
 * @date : 2022-03-08 09:42
 */
public class SimpleIdempotentIdGenerate implements IdempotentIdGenerate {

    @Override
    public String idGenerate(IdempotentIdContext context) throws Exception {
        String str = context.toString();
        if (StrUtil.isBlankIfStr(str)) {
            return StrUtil.EMPTY;
        }

        return Hashing.md5().hashBytes(str.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
