package com.aizuda.easy.retry.template.datasource.persistence.dataobject;

import lombok.Data;

/**
 * @author: www.byteblogs.com
 * @date : 2022-04-22 20:27
 */
@Data
public class DispatchQuantityResponseDO {

    private String createDt;

    private Long total;

    private Long successNum;

    private Long fail;

    private Long totalNum;

    private Long failNum;

    private Long stopNum;

    private Long cancelNum;
}
