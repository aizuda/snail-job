package com.aizuda.snailjob.template.datasource.persistence.dataobject;

import lombok.Data;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-04-22 20:27
 */
@Data
public class DashboardRetryLineResponseDO {

    /**
     * 任务列表
     */
    private List<Task> taskList;

    /**
     * 排名列表
     */
    private List<Rank> rankList;

    /**
     * 折线图列表
     */
    private List<DashboardLineResponseDO> dashboardLineResponseDOList;

    @Data
    public static class Task {
        private String groupName;

        private Integer run;

        private Integer total;
    }

    @Data
    public static class Rank {
        private String name;

        private String total;
    }
}
