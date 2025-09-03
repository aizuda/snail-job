package com.aizuda.snailjob.client.common.rpc.client.openapi;

import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import lombok.Data;

import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
@Data
public class Request {

    /**
     * {@link RequestMethod} 请求类型
     */
    private String method;

    /**
     * 路径
     */
    private String path;

    /**
     * 参数
     */
    private String body;

    /**
     * url参数
     */
    private String params;

    /**
     * 返回值类型
     */
    private Class<?> returnType;

    /**
     * 上下文
     */
    private Map<String, String> headers;

    /**
     * 超时时间
     */
    private long timeout;
}
