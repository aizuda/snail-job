package com.aizuda.easy.retry.server.web.service;

import com.aizuda.easy.retry.server.web.model.base.BaseQueryVO;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.ServerNodeQueryVO;
import com.aizuda.easy.retry.server.web.model.response.DashboardCardResponseVO;
import com.aizuda.easy.retry.server.web.model.response.DashboardRetryLineResponseVO;
import com.aizuda.easy.retry.server.web.model.response.ServerNodeResponseVO;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-04-22 20:19
 */
public interface DashBoardService {

    DashboardCardResponseVO taskRetryJob();

    DashboardRetryLineResponseVO retryLineList(BaseQueryVO baseQueryVO, String groupName, String type, String startTime, String endTime);

    DashboardRetryLineResponseVO jobLineList(BaseQueryVO baseQueryVO, String mode, String groupName, String type, String startTime, String endTime);

    PageResult<List<ServerNodeResponseVO>> pods(ServerNodeQueryVO serverNodeQueryVO);
}
