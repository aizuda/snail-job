package com.aizuda.snailjob.client.core.serializer;

import com.aizuda.snailjob.client.core.RetryArgSerializer;
import com.aizuda.snailjob.client.core.exception.RetryArgSerializeException;
import com.aizuda.snailjob.common.core.util.ForyUtil;

import java.lang.reflect.Method;

public class ForySerializer implements RetryArgSerializer {

    @Override
    public String name() {
        return "fory";
    }

    @Override
    public String serialize(Object serializeInfo) {
        return ForyUtil.serialize(serializeInfo);
    }

    @Override
    public Object deSerialize(String infoStr, Class tClass, Method method) throws RetryArgSerializeException {
        try {
            return ForyUtil.deserialize(infoStr);
        } catch (Exception e) {
            throw new RetryArgSerializeException(e);
        }
    }
}
