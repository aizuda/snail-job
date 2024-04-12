package com.aizuda.easy.retry.client.core.generator;

import cn.hutool.crypto.SecureUtil;
import com.aizuda.easy.retry.client.core.IdempotentIdGenerate;
import com.aizuda.easy.retry.common.core.model.IdempotentIdContext;

/**
 * 默认的idempotentId 生成器
 *
 * @author: opensnail
 * @date : 2022-03-08 09:42
 */
public class SimpleIdempotentIdGenerate implements IdempotentIdGenerate {

    @Override
    public String idGenerate(IdempotentIdContext context) throws Exception {
        return SecureUtil.md5(context.toString());
    }
}
