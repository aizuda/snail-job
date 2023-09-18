package com.aizuda.easy.retry.server.web.service;

import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.SceneConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.response.SceneConfigResponseVO;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 10:54
 */
public interface SceneConfigService {

   PageResult<List<SceneConfigResponseVO>> getSceneConfigPageList(SceneConfigQueryVO groupName);

   List<SceneConfigResponseVO> getSceneConfigList(String groupName);
}
