package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;


/**
 * @author: www.byteblogs.com
 * @date : 2022-04-22 20:27
 */
@Data
public class DispatchQuantityResponseVO {

    private String createDt;

    private Long total;

    private Long successNum;

    private Long fail;

    private Long totalNum;

    private Long failNum;

    private Long stopNum;

    private Long cancelNum;
}
