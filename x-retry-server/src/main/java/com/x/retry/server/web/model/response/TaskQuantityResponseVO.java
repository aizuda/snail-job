package com.x.retry.server.web.model.response;

import lombok.Data;

/**
 * @author: www.byteblogs.com
 * @date : 2022-04-22 20:27
 */
@Data
public class TaskQuantityResponseVO {

    private Long total;

    private Long running;

    private Long finish;

    private Long maxRetryCount;



}
