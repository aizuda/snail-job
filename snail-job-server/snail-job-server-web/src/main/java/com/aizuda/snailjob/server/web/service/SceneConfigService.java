package com.aizuda.snailjob.server.web.service;

import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.ExportSceneVO;
import com.aizuda.snailjob.server.web.model.request.SceneConfigQueryVO;
import com.aizuda.snailjob.server.web.model.request.SceneConfigRequestVO;
import com.aizuda.snailjob.server.web.model.response.SceneConfigResponseVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

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

    boolean updateStatus(Long id, final Integer status);

    void importSceneConfig(@Valid @NotEmpty(message = "导入数据不能为空") List<SceneConfigRequestVO> requests);

    String exportSceneConfig(ExportSceneVO exportSceneVO);

}
