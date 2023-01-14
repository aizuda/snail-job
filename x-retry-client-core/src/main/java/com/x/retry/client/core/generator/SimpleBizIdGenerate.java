package com.x.retry.client.core.generator;

import cn.hutool.crypto.SecureUtil;
import com.x.retry.client.core.BizIdGenerate;
import com.x.retry.common.core.util.JsonUtil;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-08 09:42
 */
public class SimpleBizIdGenerate implements BizIdGenerate {

    @Override
    public String idGenerate(Object... t) throws Exception {
        return SecureUtil.md5(JsonUtil.toJsonString(t));
    }
}
