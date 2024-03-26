package com.aizuda.easy.retry.server.web.controller;

import com.aizuda.easy.retry.server.common.dto.DistributeInstance;
import com.aizuda.easy.retry.server.web.annotation.LoginRequired;
import com.aizuda.easy.retry.server.web.model.base.BaseQueryVO;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.ServerNodeQueryVO;
import com.aizuda.easy.retry.server.web.model.response.DashboardCardResponseVO;
import com.aizuda.easy.retry.server.web.model.response.DashboardRetryLineResponseVO;
import com.aizuda.easy.retry.server.web.model.response.ServerNodeResponseVO;
import com.aizuda.easy.retry.server.web.service.DashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 仪表盘接口
 *
 * @author: www.byteblogs.com
 * @date : 2022-04-22 20:17
 */
@RestController
@RequestMapping("/dashboard")
public class DashBoardController {

    @Autowired
    private DashBoardService dashBoardService;

    @LoginRequired
    @GetMapping("/task-retry-job")
    public DashboardCardResponseVO taskRetryJob() {
        return dashBoardService.taskRetryJob();
    }

    @LoginRequired
    @GetMapping("/retry/line")
    public DashboardRetryLineResponseVO retryLineList(BaseQueryVO baseQueryVO,
                                                      @RequestParam(value = "groupName", required = false) String groupName,
                                                      @RequestParam(value = "type", required = false, defaultValue = "WEEK") String type,
                                                      @RequestParam(value = "startTime", required = false) String startTime,
                                                      @RequestParam(value = "endTime", required = false) String endTime
    ) {
        return dashBoardService.retryLineList(baseQueryVO, groupName, type, startTime, endTime);
    }

    @LoginRequired
    @GetMapping("/job/line")
    public DashboardRetryLineResponseVO jobLineList(BaseQueryVO baseQueryVO,
                                                    @RequestParam(value = "mode", required = false) String mode,
                                                    @RequestParam(value = "groupName", required = false) String groupName,
                                                    @RequestParam(value = "type", required = false, defaultValue = "WEEK") String type,
                                                    @RequestParam(value = "startTime", required = false) String startTime,
                                                    @RequestParam(value = "endTime", required = false) String endTime
    ) {
        return dashBoardService.jobLineList(baseQueryVO, mode, groupName, type, startTime, endTime);
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
