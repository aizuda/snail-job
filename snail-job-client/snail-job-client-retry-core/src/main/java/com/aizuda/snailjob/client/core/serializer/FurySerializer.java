package com.aizuda.snailjob.client.core.serializer;

import com.aizuda.snailjob.client.core.RetryArgSerializer;
import com.aizuda.snailjob.common.core.util.FuryUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.lang.reflect.Method;

public class FurySerializer implements RetryArgSerializer {

    @Override
    public SerializerType type() {
        return SerializerType.FURY;
    }

    @Override
    public String serialize(Object serializeInfo) {
        return FuryUtil.serialize(serializeInfo);
    }

    @Override
    public Object deSerialize(String infoStr, Class tClass, Method method) throws JsonProcessingException {
        return FuryUtil.deserialize(infoStr);
    }
}
