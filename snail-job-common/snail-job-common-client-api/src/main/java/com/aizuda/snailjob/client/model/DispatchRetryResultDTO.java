package com.aizuda.snailjob.client.model;

import lombok.Data;

/**
 * 服务端调度重试出参
 * @auther opensnail
 * @date 2022/03/25 10:06
 */
@Data
public class DispatchRetryResultDTO {
    private String resultJson;
    private Integer statusCode;
    private String idempotentId;
    private String exceptionMsg;
    private String uniqueId;
}
