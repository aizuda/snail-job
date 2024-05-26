package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.SceneConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.SceneConfigRequestVO;
import com.aizuda.snailjob.server.web.model.response.SceneConfigResponseVO;

import java.util.List;
import java.util.Set;

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

    boolean updateStatus(Long id, final Integer status);

    void importSceneConfig(List<SceneConfigRequestVO> requests);

    String exportSceneConfig(Set<Long> sceneIds);

    void batchCopy(Long targetNamespaceId, Set<Long> sceneIds);
}
