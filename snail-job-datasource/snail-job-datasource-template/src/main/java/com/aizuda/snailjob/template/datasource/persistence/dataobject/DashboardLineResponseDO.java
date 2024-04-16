package com.aizuda.snailjob.template.datasource.persistence.dataobject;

import lombok.Data;

/**
 * @author: wodeyangzipingpingwuqi
 * @date : 2023-11-30
 */
@Data
public class DashboardLineResponseDO {

    /**
     * 时间x轴
     */
    private String createDt;

    /**
     * 总量，计算百分比
     */
    private Long total;

    /**
     * 重试-成功数
     */
    private Long successNum;

    /**
     * 重试-运行数
     */
    private Long runningNum;

    /**
     * 重试-最大次数
     */
    private Long maxCountNum;

    /**
     * 重试-暂停数
     */
    private Long suspendNum;

    /**
     * 定时-失败任务
     */
    private Long fail;

    /**
     * 定时-总任务数
     */
    private Long totalNum;

    /**
     * 定时-无效任务数
     */
    private Long failNum;

    /**
     * 定时-停止数
     */
    private Long stop;

    /**
     * 定时-取消数
     */
    private Long cancel;

    /**
     * 定时-成功数
     */
    private Long success;

}
