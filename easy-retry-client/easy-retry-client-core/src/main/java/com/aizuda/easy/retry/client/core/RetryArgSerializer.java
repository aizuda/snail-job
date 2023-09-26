package com.aizuda.easy.retry.client.core;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.lang.reflect.Method;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-07 15:08
 */
public interface RetryArgSerializer {

    String serialize(Object serializeInfo);

    Object deSerialize(String infoStr, Class tClass, Method method) throws JsonProcessingException;
}
