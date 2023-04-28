package com.aizuda.easy.retry.client.core.generator;

import cn.hutool.crypto.SecureUtil;
import com.aizuda.easy.retry.client.core.BizIdGenerate;
import com.aizuda.easy.retry.common.core.util.JsonUtil;

/**
 * 默认的bizId 生成器
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-08 09:42
 */
public class SimpleBizIdGenerate implements BizIdGenerate {

    @Override
    public String idGenerate(Object... t) throws Exception {
        return SecureUtil.md5(JsonUtil.toJsonString(t));
    }
}
