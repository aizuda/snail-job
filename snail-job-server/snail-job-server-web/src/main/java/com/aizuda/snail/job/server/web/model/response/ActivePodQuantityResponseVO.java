package com.aizuda.snail.job.server.web.model.response;

import lombok.Data;

/**
 * @author: opensnail
 * @date : 2022-04-23 10:39
 */
@Data
public class ActivePodQuantityResponseVO {

    private Long total;

    private Long clientTotal;

    private Long serverTotal;
}
