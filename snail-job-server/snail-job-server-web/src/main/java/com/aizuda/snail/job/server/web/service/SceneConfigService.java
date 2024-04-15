package com.aizuda.snail.job.server.web.service;

import com.aizuda.snail.job.server.web.model.base.PageResult;
import com.aizuda.snail.job.server.web.model.request.SceneConfigQueryVO;
import com.aizuda.snail.job.server.web.model.request.SceneConfigRequestVO;
import com.aizuda.snail.job.server.web.model.response.SceneConfigResponseVO;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-03-03 10:54
 */
public interface SceneConfigService {

   PageResult<List<SceneConfigResponseVO>> getSceneConfigPageList(SceneConfigQueryVO groupName);

   List<SceneConfigResponseVO> getSceneConfigList(String groupName);

    Boolean saveSceneConfig(SceneConfigRequestVO requestVO);

    Boolean updateSceneConfig(SceneConfigRequestVO requestVO);

    SceneConfigResponseVO getSceneConfigDetail(Long id);
}
