package com.aizuda.snailjob.client.model.request;

import lombok.Data;

/**
 * @author: opensnail
 * @date : 2023-09-26 15:10
 */
@Data
public class DispatchRetryResultRequest {

    private String namespaceId;
    private String groupName;
    private String sceneName;
    private Long retryId;
    private Long retryTaskId;

    private String result;
    private Integer statusCode;
    private String exceptionMsg;
}
