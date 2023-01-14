package com.x.retry.server.service;

import com.x.retry.server.web.model.response.ActivePodQuantityResponseVO;
import com.x.retry.server.web.model.response.DispatchQuantityResponseVO;
import com.x.retry.server.web.model.response.SceneQuantityRankResponseVO;
import com.x.retry.server.web.model.response.TaskQuantityResponseVO;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-04-22 20:19
 */
public interface DashBoardService {

    TaskQuantityResponseVO countTask();

    DispatchQuantityResponseVO countDispatch();

    ActivePodQuantityResponseVO countActivePod();

    List<SceneQuantityRankResponseVO> rankSceneQuantity(String groupName);

    List<DispatchQuantityResponseVO> lineDispatchQuantity(String groupName);

}
