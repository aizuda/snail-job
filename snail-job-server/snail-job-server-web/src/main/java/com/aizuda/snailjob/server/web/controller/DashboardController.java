package com.aizuda.snailjob.server.web.controller;

import com.aizuda.snailjob.server.common.dto.DistributeInstance;
import com.aizuda.snailjob.server.web.annotation.LoginRequired;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobLineQueryVo;
import com.aizuda.snailjob.server.web.model.request.LineQueryVO;
import com.aizuda.snailjob.server.web.model.request.ServerNodeQueryVO;
import com.aizuda.snailjob.server.web.model.response.DashboardCardResponseVO;
import com.aizuda.snailjob.server.web.model.response.DashboardRetryLineResponseVO;
import com.aizuda.snailjob.server.web.model.response.ServerNodeResponseVO;
import com.aizuda.snailjob.server.web.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 仪表盘接口
 *
 * @author: opensnail
 * @date : 2022-04-22 20:17
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashBoardService;

    @LoginRequired
    @GetMapping("/task-retry-job")
    public DashboardCardResponseVO taskRetryJob() {
        return dashBoardService.taskRetryJob();
    }

    @LoginRequired
    @GetMapping("/retry/line")
    public DashboardRetryLineResponseVO retryLineList(LineQueryVO queryVO) {
        return dashBoardService.retryLineList(queryVO);
    }

    @LoginRequired
    @GetMapping("/job/line")
    public DashboardRetryLineResponseVO jobLineList(JobLineQueryVo queryVO) {
        return dashBoardService.jobLineList(queryVO);
    }

    @LoginRequired
    @GetMapping("/pods")
    public PageResult<List<ServerNodeResponseVO>> pods(ServerNodeQueryVO serverNodeQueryVO) {
        return dashBoardService.pods(serverNodeQueryVO);
    }

    @GetMapping("/consumer/bucket")
    public Set<Integer> allConsumerGroupName() {
        return DistributeInstance.INSTANCE.getConsumerBucket();
    }

}
