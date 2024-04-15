package com.aizuda.snail.job.template.datasource.persistence.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author: wodeyangzipingpingwuqi
 * @date : 2023-11-29
 */
@Data
public class DashboardCardResponseDO {

    /**
     * 定时任务
     */
    private JobTask jobTask;

    /**
     * 重试任务
     */
    private RetryTask retryTask;

    @Data
    public static class RetryTask {

        // 总数
        private Long totalNum;

        // 运行中
        private Long runningNum;

        // 完成
        private Long finishNum;

        // 最大重试次数
        private Long maxCountNum;

        // 暂停重试
        private Long suspendNum;

        // 触发时间
        private LocalDateTime triggerAt;
    }

    @Data
    public static class JobTask {
        //成功
        private Integer successNum;
        //失败
        private Integer failNum;
        //取消
        private Integer cancelNum;
        //停止
        private Integer stopNum;
        // 总数
        private Integer totalNum;
        // 成功率
        private BigDecimal successRate;
    }
}
