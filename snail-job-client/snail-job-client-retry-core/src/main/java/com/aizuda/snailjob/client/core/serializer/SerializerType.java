package com.aizuda.snailjob.client.core.serializer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SerializerType {

    CUSTOM(1),

    JACKSON(2),

    HESSIAN(3),

    FURY(4);

    @Getter
    private final Integer code;
}
