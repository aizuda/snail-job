package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;

/**
 * @author: www.byteblogs.com
 * @date : 2022-04-23 10:39
 */
@Data
public class ActivePodQuantityResponseVO {

    private Long total;

    private Long clientTotal;

    private Long serverTotal;
}
