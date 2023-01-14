package com.x.retry.server.service.convert;

import com.x.retry.common.core.covert.AbstractConverter;
import com.x.retry.server.persistence.mybatis.po.SceneConfig;
import com.x.retry.server.web.model.response.SceneConfigResponseVO;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 11:14
 */
public class SceneConfigResponseVOConverter extends AbstractConverter<SceneConfig, SceneConfigResponseVO> {

    @Override
    public SceneConfigResponseVO convert(SceneConfig sceneConfig) {
        return convert(sceneConfig, SceneConfigResponseVO.class);
    }

    @Override
    public List<SceneConfigResponseVO> batchConvert(List<SceneConfig> sceneConfigs) {
        return batchConvert(sceneConfigs, SceneConfigResponseVO.class);
    }
}
