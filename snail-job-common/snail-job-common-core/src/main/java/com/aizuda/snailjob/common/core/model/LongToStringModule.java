package com.aizuda.snailjob.common.core.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

/**
 * @author fangzhengjin
 * @date 2025-06-09 16:08
 */
public class LongToStringModule extends SimpleModule {
    public LongToStringModule() {
        super("LongToStringModule");
        this.addSerializer(Long.class, new JsonSerializer<>() {
            @Override
            public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.toString());
            }
        });
        this.addSerializer(Long.TYPE, new JsonSerializer<>() {
            @Override
            public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.toString());
            }
        });
    }
}
