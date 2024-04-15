package com.aizuda.snail.job.server.web.model.response;

import lombok.Data;

/**
 * @author: opensnail
 * @date : 2022-04-22 20:27
 */
@Data
public class TaskQuantityResponseVO {

    private Long total;

    private Long running;

    private Long finish;

    private Long maxRetryCount;



}
