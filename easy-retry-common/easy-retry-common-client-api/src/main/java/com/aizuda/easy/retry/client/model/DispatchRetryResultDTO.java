package com.aizuda.easy.retry.client.model;

import lombok.Data;

/**
 * 服务端调度重试出参
 * @auther www.byteblogs.com
 * @date 2022/03/25 10:06
 */
@Data
public class DispatchRetryResultDTO {
    private String resultJson;
    private Integer statusCode;
    private String bizId;
    private String exceptionMsg;
}
