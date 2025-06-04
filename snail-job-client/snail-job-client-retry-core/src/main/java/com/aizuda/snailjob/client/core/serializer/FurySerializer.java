package com.aizuda.snailjob.client.core.serializer;

import com.aizuda.snailjob.client.core.RetryArgSerializer;
import com.aizuda.snailjob.client.core.exception.RetryArgSerializeException;
import com.aizuda.snailjob.common.core.util.FuryUtil;

import java.lang.reflect.Method;

public class FurySerializer implements RetryArgSerializer {

    @Override
    public String name() {
        return "fury";
    }

    @Override
    public String serialize(Object serializeInfo) {
        return FuryUtil.serialize(serializeInfo);
    }

    @Override
    public Object deSerialize(String infoStr, Class tClass, Method method) throws RetryArgSerializeException {
        try {
            return FuryUtil.deserialize(infoStr);
        } catch (Exception e) {
            throw new RetryArgSerializeException(e);
        }
    }
}
