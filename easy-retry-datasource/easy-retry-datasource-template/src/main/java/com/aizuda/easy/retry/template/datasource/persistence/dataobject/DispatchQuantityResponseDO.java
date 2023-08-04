package com.aizuda.easy.retry.template.datasource.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: www.byteblogs.com
 * @date : 2022-04-22 20:27
 */
@Data
public class DispatchQuantityResponseDO {

    private Long total;

    private BigDecimal successPercent = BigDecimal.ZERO;

    private Long success;

    private Long fail;

    private String createDt;

}
