package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author: wodeyangzipingpingwuqi
 * @date : 2023-11-29
 */
@Data
public class DashboardCardResponseVO {

    /**
     * 定时任务
     */
    private JobTask jobTask;

    /**
     * 工作流任务
     */
    private WorkFlowTask workFlowTask;

    /**
     * 重试任务
     */
    private RetryTask retryTask;

    /**
     * 重试任务折线图
     */
    private List<RetryTaskBar> retryTaskBarList;

    /**
     * 在线服务
     */
    private OnLineService onLineService = new OnLineService();

    @Data
    public static class OnLineService {
        private Long total;

        private Long clientTotal;

        private Long serverTotal;
    }

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

    @Data
    public static class WorkFlowTask {
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

    @Data
    @Accessors(chain = true)
    public static class RetryTaskBar {
        /**
         * 时间x轴
         */
        private String x;

        /**
         * 任务总数y轴
         */
        private Long taskTotal;
    }
}
