package com.x.retry.server.service.convert;

import com.x.retry.common.core.covert.AbstractConverter;
import com.x.retry.server.persistence.mybatis.po.SceneConfig;
import com.x.retry.server.web.model.request.GroupConfigRequestVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-26 13:49
 */
public class SceneConfigConverter extends AbstractConverter<GroupConfigRequestVO.SceneConfigVO, SceneConfig> {

    @Override
    public SceneConfig convert(GroupConfigRequestVO.SceneConfigVO sceneConfigVO) {
        SceneConfig sceneConfig = convert(sceneConfigVO, SceneConfig.class);
        sceneConfig.setUpdateDt(LocalDateTime.now());
        return sceneConfig;
    }

    @Override
    public List<SceneConfig> batchConvert(List<GroupConfigRequestVO.SceneConfigVO> sceneConfigVOS) {
        return null;
    }
}
