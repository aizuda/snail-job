package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;

/**
 * @author: wodeyangzipingpingwuqi
 * @date : 2023-11-30
 */
@Data
public class DashboardRetryLinkeResponseVO {

    private String createDt;

    private Long total;

    private Long successNum;

    private Long runningNum;

    private Long maxCountNum;

    private Long suspendNum;
}
