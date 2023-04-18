package com.aizuda.easy.retry.server.service.convert;

import com.aizuda.easy.retry.server.persistence.mybatis.po.NotifyConfig;
import com.aizuda.easy.retry.common.core.covert.AbstractConverter;
import com.aizuda.easy.retry.server.web.model.response.NotifyConfigResponseVO;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 11:20
 */
public class NotifyConfigResponseVOConverter extends AbstractConverter<NotifyConfig, NotifyConfigResponseVO> {

    @Override
    public NotifyConfigResponseVO convert(NotifyConfig notifyConfig) {
        return convert(notifyConfig, NotifyConfigResponseVO.class);
    }

    @Override
    public List<NotifyConfigResponseVO> batchConvert(List<NotifyConfig> notifyConfigs) {
        return batchConvert(notifyConfigs, NotifyConfigResponseVO.class);
    }
}
