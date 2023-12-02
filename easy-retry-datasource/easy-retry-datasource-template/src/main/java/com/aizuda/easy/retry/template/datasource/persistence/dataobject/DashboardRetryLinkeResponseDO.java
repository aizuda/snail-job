package com.aizuda.easy.retry.template.datasource.persistence.dataobject;

import lombok.Data;

/**
 * @author: wodeyangzipingpingwuqi
 * @date : 2023-11-30
 */
@Data
public class DashboardRetryLinkeResponseDO {

    private String createDt;

    private Long total;

    private Long successNum;

    private Long runningNum;

    private Long maxCountNum;

    private Long suspendNum;
}
