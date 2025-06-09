package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.server.web.model.response.DashboardCardResponseVO;
import com.aizuda.snailjob.server.web.model.response.DashboardRetryLineResponseVO;
import com.aizuda.snailjob.server.web.model.response.ServerNodeResponseVO;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-04-22 20:19
 */
public interface DashboardService {

    DashboardCardResponseVO taskRetryJob();

    DashboardRetryLineResponseVO retryLineList(LineQueryVO queryVO);

    DashboardRetryLineResponseVO jobLineList(JobLineQueryVo queryVO);

    PageResult<List<ServerNodeResponseVO>> pods(ServerNodeQueryVO serverNodeQueryVO);

    Boolean updatePodsStatus(ServerNodeStatusUpdateRequestVO updateRequestVO);

    Boolean updatePodsLabels(ServerNodeLabelsUpdateRequestVO updateRequestVO);
}
