package com.aizuda.snailjob.server.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 重试上报DTO
 *
 * @author: opensnail
 * @date : 2021-11-25 14:11
 */
@Data
public class RetryTaskDTO implements Serializable {

    /**
     * groupName
     */
    private String groupName;

    /**
     * sceneName
     */
    private String sceneName;

    /**
     * 业务唯一id
     */
    private String idempotentId;

    /**
     * 执行器名称
     */
    private String executorName;

    /**
     * 业务唯一编号
     */
    private String bizNo;

    /**
     * 客户端上报参数
     */
    private String argsStr;

    /**
     * 额外扩展参数
     */
    private String extAttrs;

    /**
     * 序列化类型 {@link com.aizuda.snailjob.client.core.serializer.SerializerType SerializerType}
     */
    private int argsSerializerType;
}
