package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: www.byteblogs.com
 * @date : 2022-04-22 20:27
 */
@Data
public class DispatchQuantityResponseVO {

    private Long total;

    private BigDecimal successPercent = BigDecimal.ZERO;

    private Long success;

    private Long fail;

    private String createDt;

}
