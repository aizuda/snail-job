package com.x.retry.client.core.client.response;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author www.byteblogs.com
 * @date 2022-03-08
 * @since 2.0
 */
public class ResponsePool {

    private ConcurrentMap<String, Object> futureResponsePool = new ConcurrentHashMap<String, Object>();




}
