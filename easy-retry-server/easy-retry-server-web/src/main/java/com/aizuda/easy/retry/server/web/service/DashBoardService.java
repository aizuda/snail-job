package com.aizuda.easy.retry.server.web.service;

import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.ServerNodeQueryVO;
import com.aizuda.easy.retry.server.web.model.response.ActivePodQuantityResponseVO;
import com.aizuda.easy.retry.server.web.model.response.DispatchQuantityResponseVO;
import com.aizuda.easy.retry.server.web.model.response.SceneQuantityRankResponseVO;
import com.aizuda.easy.retry.server.web.model.response.ServerNodeResponseVO;
import com.aizuda.easy.retry.server.web.model.response.TaskQuantityResponseVO;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-04-22 20:19
 */
public interface DashBoardService {

    TaskQuantityResponseVO countTask();

    DispatchQuantityResponseVO countDispatch();

    ActivePodQuantityResponseVO countActivePod();

    List<SceneQuantityRankResponseVO> rankSceneQuantity(String groupName, String type, String startTime, String endTime);

    List<DispatchQuantityResponseVO> lineDispatchQuantity(String groupName, String type, String startTime, String endTime);

    PageResult<List<ServerNodeResponseVO>> pods(ServerNodeQueryVO serverNodeQueryVO);
}
