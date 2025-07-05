package com.aizuda.snailjob.server.web.model.response;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import lombok.Data;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-04-22 20:27
 */
@Data
public class DashboardRetryLineResponseVO {

    /**
     * 任务列表
     */
    private PageResult<List<Task>> taskList;

    /**
     * 排名列表
     */
    private List<Rank> rankList;

    /**
     * 折线图列表
     */
    private List<DashboardLineResponseVO> dashboardLineResponseDOList;

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
